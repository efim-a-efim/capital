package dev.capital

import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppTest {
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val device=UiDevice.getInstance(instrumentation)
    private fun find(text: String): UiObject2 {
        repeat(8) {
            device.wait(Until.findObject(By.text(text)),600)?.let { return it }
            device.findObject(By.desc(text))?.let { return it }
            device.findObjects(By.scrollable(true)).firstOrNull()?.scroll(Direction.DOWN,0.7f)
        }
        val output=java.io.ByteArrayOutputStream(); device.dumpWindowHierarchy(output)
        error("Cannot find $text; hierarchy: $output")
    }
    private fun click(text: String) { find(text).click(); device.waitForIdle() }
    private fun type(label: String,value: String) {
        val key=mapOf("Name" to "name", "Purpose" to "name", "Currency code (EUR, USD, BTC…)" to "currency", "Current quantity · no grouping separators" to "quantity", "Target amount · no grouping separators" to "target", "Amount · no grouping separators" to "amount").getValue(label)
        var found: UiObject2?=null
        for(attempt in 0..6) {
            found=device.wait(Until.findObject(By.res(key)),600)
            if(found!=null) break
            device.findObjects(By.scrollable(true)).firstOrNull()?.scroll(Direction.DOWN,0.6f)
        }
        val node=found ?: run { val output=java.io.ByteArrayOutputStream(); device.dumpWindowHierarchy(output); error("No input $key: $output") }
        node.click(); node.text=value; device.waitForIdle()
    }
    private fun capture(name: String) { device.takeScreenshot(File(instrumentation.targetContext.getExternalFilesDir(null),"$name.png")) }
    @Test fun localFolderCrudAndReopen() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            if(device.wait(Until.findObject(By.text("Choose or reopen folder")),4000)==null) {
                click("Settings"); click("Reconnect / open folder")
            } else click("Choose or reopen folder")
            assertTrue(device.wait(Until.hasObject(By.pkg("com.google.android.documentsui")),10000))
            val newFolder=device.wait(Until.findObject(By.desc("New folder")),3000)
            if(newFolder!=null) newFolder.click() else {
                device.findObject(By.desc("More options"))?.click()
                device.wait(Until.findObject(By.text("New folder")),3000)?.click()
            }
            val input=device.wait(Until.findObject(By.clazz("android.widget.EditText")),3000)
            assertNotNull("Folder creation dialog",input)
            input.text="CapitalTest-${System.currentTimeMillis()}"
            device.findObject(By.text("OK"))?.click() ?: device.findObject(By.text("Create"))?.click()
            device.wait(Until.findObject(By.textContains("USE THIS FOLDER")),3000)?.click() ?: device.findObject(By.textContains("Use this folder"))?.click()
            device.wait(Until.findObject(By.text("ALLOW")),3000)?.click() ?: device.findObject(By.text("Allow"))?.click()
            click("Overview")
            click("Add bucket"); type("Name","Reserve"); type("Currency code (EUR, USD, BTC…)","USD"); click("Save")
            click("Reserve"); click("Add holding"); type("Name","Cash"); type("Currency code (EUR, USD, BTC…)","USD"); type("Current quantity · no grouping separators","900"); click("Save")
            find("Cash"); capture("bucket")
            click("Goals"); click("Add goal"); type("Purpose","Emergency"); type("Currency code (EUR, USD, BTC…)","USD"); type("Target amount · no grouping separators","600"); click("Save")
            click("Emergency"); click("Connect bucket"); capture("connection"); click("Save"); find("Disconnect"); capture("goal")
            click("Plans"); click("Add planned saving"); type("Name","Salary"); type("Currency code (EUR, USD, BTC…)","USD"); type("Amount · no grouping separators","100"); click("Save"); find("Salary")
            var data=Portfolio(); var tree: Uri?=null
            scenario.onActivity { activity ->
                val model=ViewModelProvider(activity)[CapitalModel::class.java]
                data=model.state.value.data; tree=Uri.parse(model.state.value.folder)
                assertEquals("900",data.holdings.single().quantity)
                assertEquals(0,data.allocate().goal(data.goals.single().id).compareTo("600".toBigDecimal()))
                assertFalse(model.state.value.unsaved)
            }
            val resolver=instrumentation.targetContext.contentResolver
            val store=FolderStore(resolver,tree!!)
            runBlocking {
                assertEquals(data,store.scan().heads.single().data)
                val root=DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree))
                val broken=DocumentsContract.createDocument(resolver,root,"application/json","capital-interrupted.json")!!
                resolver.openOutputStream(broken)!!.use { it.write("partial".toByteArray()) }
                assertEquals(1,store.scan().invalid); assertEquals(data,store.scan().heads.single().data)
            }
            scenario.recreate(); find("Salary"); click("Buckets"); find("Reserve"); click("Overview"); find("Emergency"); capture("overview")
        }
    }
}
