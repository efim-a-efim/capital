---
layout: default
lang: mr
base: "/mr"
key: "accounts"
title: ब्रोकर आणि फॉरेक्स खाती
class: doc
---
# ब्रोकर आणि फॉरेक्स खाती

Capital ब्रोकरेज किंवा फॉरेक्स खात्याचे एकूण मूल्य क्रिप्टो वॉलेटप्रमाणेच वाचू शकते. तुम्ही खाते कप्प्यात **ब्रोकर खाते** प्रकारचे होल्डिंग म्हणून जोडता, आणि प्रत्येक रिफ्रेशला अॅप खात्याचे नेट अॅसेट व्हॅल्यू (NAV) खात्याच्या बेस चलनात मिळवते. अॅप फक्त वाचते: ते ब्रोकरचा रिपोर्टिंग इंटरफेस आणि तुम्ही स्वतः तयार केलेला टोकन वापरते, ते कधीही ऑर्डर देत नाही, बदलत नाही किंवा रद्द करत नाही, आणि पैसे हलवत नाही.

Capital फक्त अशा इंटरफेसशी जोडते ज्यांची क्रेडेन्शियल्स दीर्घायुषी आहेत: तुम्ही एकदा तयार केलेला टोकन किंवा की, जो तुम्ही रद्द करेपर्यंत, तुम्ही निवडलेल्या मुदतीपर्यंत किंवा किमान काही महिने वैध राहतो (T-Invest टोकन तीन महिने न वापरल्यास निष्क्रिय होतात, ALOR टोकन एका वर्षानंतर). खालील प्रत्येक ब्रोकर अॅपची भाषा कोणतीही असो उपलब्ध आहे. आज समर्थित:

| ब्रोकर | वापरलेला इंटरफेस | काय वाचले जाते |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (फक्त रिपोर्ट मिळवणे) | शेवटच्या व्यावसायिक दिवशीचे नेट अॅसेट व्हॅल्यू, खात्याच्या बेस चलनात |
| [OANDA](#oanda) | v20 REST API, fxTrade लाइव्ह खाती | रिफ्रेशच्या वेळचे नेट अॅसेट व्हॅल्यू, खात्याच्या चलनात |
| [Trading 212](#trading-212) | Public API, Invest आणि Stocks ISA खाती | रिफ्रेशच्या वेळचे खात्याचे एकूण मूल्य, खात्याच्या मुख्य चलनात |
| [SnapTrade](#snaptrade) | SnapTrade Personal, अनेक ब्रोकर समाविष्ट करणारा अॅग्रिगेटर | ब्रोकर SnapTrade ला कळवतो तसे खात्याचे एकूण मूल्य, खात्याच्या चलनात |
| [Alpaca](#alpaca) | Trading API, लाइव्ह खाती | इक्विटी (रोख अधिक पोझिशन्स), अमेरिकी डॉलरमध्ये |
| [Tradier](#tradier) | Brokerage API | एकूण इक्विटी, अमेरिकी डॉलरमध्ये |
| [tastytrade](#tastytrade) | वैयक्तिक OAuth ग्रँटसह Open API | नेट लिक्विडेटिंग व्हॅल्यू, अमेरिकी डॉलरमध्ये |
| [Public.com](#public) | Individual API | खात्याचे एकूण मूल्य, अमेरिकी डॉलरमध्ये |
| [eToro](#etoro) | Public API | निवडलेल्या खात्याची शिल्लक (ट्रेडिंग खात्यासाठी: रोख अधिक गुंतवलेल्या पोझिशन्स), त्याच्या चलनात |
| [Indexa Capital](#indexa-capital) | REST API, फक्त-वाचन टोकन | शेवटच्या मूल्यांकन तारखेचे पोर्टफोलिओ एकूण, खात्याच्या चलनात |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | पोर्टफोलिओचे एकूण मूल्य, रूबलमध्ये |
| [ALOR](#alor) | ALOR OpenAPI | मॉस्को एक्स्चेंजवरील पोर्टफोलिओ मूल्यांकन, रूबलमध्ये |
| [Capital.com](#capital-com) | Public API, लाइव्ह खाती | खुल्या नफा-तोट्यासह शिल्लक, खात्याच्या चलनात |
| [Akahu](#akahu) | Akahu वैयक्तिक अॅप, न्यूझीलंडचा अॅग्रिगेटर | जोडलेल्या खात्याची शिल्लक (Sharesies, Hatch, Kernel, KiwiSaver आणि इतर), त्याच्या चलनात |

## सुरू करण्यापूर्वी {#before-you-start}

- **डिव्हाइसबाहेर काय जाते.** प्रत्येक रिफ्रेशला अॅप तुमचा अॅक्सेस टोकन आणि खाते किंवा क्वेरी id त्या ब्रोकरला HTTPS द्वारे पाठवते. कोणत्याही विनंतीप्रमाणे ब्रोकरला तुमचा IP पत्ता दिसतो.
- **क्रेडेन्शियल्स कुठे ठेवली जातात.** ब्रोकर्स टॅब → क्रेडेन्शियल्स. ती Android Keystore मधील कीने एन्क्रिप्ट केली जातात, तुमच्या डेटा फोल्डरमध्ये कधीही लिहिली जात नाहीत, आणि एक्सपोर्ट व सिस्टम बॅकअपमधून वगळली जातात. एका ब्रोकरची एक क्रेडेन्शियल्स-जोडी त्या ब्रोकरच्या तुम्ही जोडलेल्या सर्व खात्यांसाठी चालते.
- **तुमच्या फोल्डरमध्ये काय साठवले जाते.** खाते id, वाचलेले शेवटचे मूल्य आणि ते केव्हा वाचले. ब्रोकरकडून दुसरे काहीही नाही.
- **ब्रोकरच्या स्क्रीन बदलू शकतात.** खालील पायऱ्या ऑक्टोबर 2026 मधील ब्रोकरच्या वेबसाइटशी जुळतात. ब्रोकर वेळोवेळी मेनूंची नावे बदलतात आणि सेटिंग्ज हलवतात, म्हणून पायऱ्या पाळताना एखादी पायरी थोडी वेगळी दिसू शकते. प्रत्येक विभागात दिलेले ब्रोकरचे स्वतःचे दस्तऐवज हाच अधिकृत स्रोत आहे: येथील एखादी पायरी जुळत नसेल, तर ब्रोकरच्या पानावर तीच संज्ञा शोधा.

## Interactive Brokers {#interactive-brokers}

Capital **Flex Web Service** वापरते; हा Interactive Brokers चा पूर्वकॉन्फिगर केलेले रिपोर्ट मिळवण्याचा इंटरफेस आहे. त्यासाठीचा टोकन फक्त रिपोर्ट तयार करू आणि डाउनलोड करू शकतो; तो लॉगिन, ट्रेडिंग किंवा पैसे काढणे करू शकत नाही. Capital Activity Flex Query मधील **Net Asset Value (NAV) Summary in Base** मागते आणि सर्वात ताज्या report date चा एकूण आकडा घेते, म्हणजे मूल्य शेवटच्या व्यावसायिक दिवसाच्या क्लोजचे असते.

### 1. Flex Query तयार करा

1. [Client Portal](https://www.interactivebrokers.com/portal) मध्ये लॉग इन करा आणि **Performance & Reports → Flex Queries** (कार्यप्रदर्शन आणि अहवाल) उघडा (काही खात्यांवर मेनूचे नाव *Reporting* असते).
2. **Activity Flex Query** खाली **+** (Create) दाबा. क्वेरीला नाव द्या, उदाहरणार्थ `Capital`.
3. **Sections** (विभाग) यादीत नेमके हे दोन विभाग आणि फील्ड सुरू करा (विभागातील सर्व फील्ड निवडले तरी चालते):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. **Delivery Configuration** मध्ये **Format** ला `XML` आणि **Period** ला `Last Business Day` सेट करा. बाकीचे पर्याय डीफॉल्टवर ठेवता येतात.
5. क्वेरी सेव्ह करा, मग तिच्या शेजारील **i** (माहिती) आयकॉन दाबा आणि **Query ID** लिहून घ्या; ही एक संख्या आहे.

क्वेरीत एकच खाते असले पाहिजे. तुमची लिंक केलेली खाती किंवा अॅडव्हायझर रचना असल्यास, प्रत्येक खात्यासाठी स्वतंत्र क्वेरी तयार करा आणि ती तयार करताना फक्त तेच खाते निवडा.

### 2. Flex Web Service सुरू करा आणि टोकन तयार करा

1. त्याच **Flex Queries** पानावर **Flex Web Service Configuration** उघडा.
2. **Flex Web Service Status** चालू करा आणि सेव्ह करा. एक टोकन तयार होतो.
3. टोकन किती काळ वैध राहील ते ठरवण्यासाठी **Generate New Token** दाबा: 6 तासांपासून 1 वर्षापर्यंत. फोनचा पत्ता बदलत राहतो, म्हणून **Valid for IP address** रिकामे ठेवा. नवीन टोकन तयार केल्यावर आधीचा अवैध होतो.
4. टोकन कॉपी करा.

### 3. Capital मध्ये जोडा

1. **ब्रोकर्स → क्रेडेन्शियल्स → अॅक्सेस टोकन: Interactive Brokers**, टोकन पेस्ट करा आणि सेव्ह करा.
2. **ब्रोकर्स → +**: नाव टाका, **ब्रोकर** ला Interactive Brokers सेट करा, **Flex Query id** टाका आणि सेव्ह करा.
3. कप्पा उघडा, **होल्डिंग जोडा**, **ट्रॅकिंग** ला **ब्रोकर खाते** सेट करा, खाते निवडा आणि सेव्ह करा.
4. **रिफ्रेश करा** दाबा. पहिल्या वेळी अर्धा मिनिट लागू शकतो, कारण रिपोर्ट मागणीनुसार तयार होतो.

टोकनची मुदत संपली की रिफ्रेश *टोकनची मुदत संपली आहे; Client Portal मध्ये नवीन टोकन तयार करा* असे कळवतो: नवीन टोकन तयार करा आणि क्रेडेन्शियल्स मध्ये पेस्ट करा. Interactive Brokers एका टोकनवरून प्रति सेकंद एक आणि प्रति मिनिट दहा रिपोर्ट विनंत्या स्वीकारते; रिफ्रेश ही मर्यादा कधीही ओलांडत नाही.

Interactive Brokers दस्तऐवज: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital OANDA v20 REST API चा **account summary** कॉल करते आणि खात्याचे NAV (शिल्लक अधिक अवास्तव नफा किंवा तोटा) खात्याच्या चलनात साठवते. फक्त लाइव्ह **fxTrade** खाती समर्थित आहेत; सराव (practice) खाती बचत नसतात.

**OANDA चा वैयक्तिक अॅक्सेस टोकन फक्त-वाचन नसतो.** तो तुमच्या लॉगिनच्या प्रत्येक उप-खात्यासाठी संपूर्ण API प्रवेश देतो, ट्रेडिंगसह. Capital फक्त account summary कॉल करते, पण जो कोणी हा टोकन मिळवेल तो त्याने ट्रेड करू शकतो. त्याला पासवर्डसारखे वागवा: तो फक्त Capital मध्ये पेस्ट करा, आणि फोन हरवल्यास OANDA पोर्टलमध्ये तो रद्द (revoke) करा.

### 1. टोकन तयार करा

1. तुमच्या OANDA fxTrade खाते व्यवस्थापन पोर्टलमध्ये लॉग इन करा.
2. **My Services → Manage API Access** उघडा (जुन्या पोर्टलवर: *My Account → My Services → Manage API Access*).
3. API परवाना स्वीकारा आणि **Generate** दाबा. टोकन कॉपी करा; OANDA तो पुन्हा दाखवत नाही. तो हरवल्यास तिथे रद्द करा आणि नवीन तयार करा.

### 2. खाते id शोधा

v20 खाते id चे स्वरूप `001-001-1234567-001` असे असते, हायफनसह. तो त्याच पोर्टलमध्ये प्रत्येक उप-खात्याशेजारी दिलेला असतो, आणि fxTrade प्लॅटफॉर्मवर खात्याच्या तपशिलांत.

### 3. Capital मध्ये जोडा

1. **ब्रोकर्स → क्रेडेन्शियल्स → अॅक्सेस टोकन: OANDA**, टोकन पेस्ट करा आणि सेव्ह करा.
2. **ब्रोकर्स → +**: नाव टाका, **ब्रोकर** ला OANDA सेट करा, **OANDA खाते id** टाका आणि सेव्ह करा.
3. कप्पा उघडा, **होल्डिंग जोडा**, **ट्रॅकिंग** ला **ब्रोकर खाते** सेट करा, खाते निवडा आणि सेव्ह करा.
4. **रिफ्रेश करा** दाबा.

ज्या मार्जिन खात्याचे NAV ऋण आहे ते बचत म्हणून न मोजता त्रुटी म्हणून कळवले जाते.

OANDA दस्तऐवज: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital Trading 212 Public API चा **account summary** कॉल करते आणि खात्याचे एकूण मूल्य खात्याच्या मुख्य चलनात साठवते. API **Invest** आणि **Stocks ISA** खाती समाविष्ट करते; की-जोडी एका खात्याची असते आणि Capital एकच की-जोडी ठेवते, म्हणून ते एक Trading 212 खाते वाचते.

### 1. API की तयार करा

1. Trading 212 अॅपमध्ये किंवा वेबसाइटवर मेनू (**☰**) → **Settings** → **API (Beta)** उघडा आणि जोखमीची सूचना स्वीकारा.
2. **Generate API key** दाबा. नाव द्या, फक्त **Account data** परवानगी (वाचन) ठेवा, आणि IP प्रवेश *Unrestricted* निवडा (फोनचा पत्ता बदलत राहतो).
3. सबमिट करा. दोन्ही मूल्ये कॉपी करा: **API Key** आणि **API Secret Key**. सिक्रेट एकदाच दाखवले जाते; ते हरवल्यास की हटवा आणि नवीन जोडी तयार करा.

### 2. Capital मध्ये जोडा

1. **ब्रोकर्स → क्रेडेन्शियल्स → API की: Trading 212** आणि **API सिक्रेट: Trading 212**, प्रत्येक मूल्य पेस्ट करा.
2. **ब्रोकर्स → +**: नाव टाका, **ब्रोकर** ला Trading 212 सेट करा, **Trading 212 खाते क्रमांक** टाका (अॅपमध्ये दिसणारा खाते id, फक्त अंक) आणि सेव्ह करा.
3. कप्पा उघडा, **होल्डिंग जोडा**, **ट्रॅकिंग** ला **ब्रोकर खाते** सेट करा, खाते निवडा आणि सेव्ह करा.
4. **रिफ्रेश करा** दाबा. Trading 212 दर 5 सेकंदाला एक summary विनंती स्वीकारते.

Trading 212 दस्तऐवज: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) हा अॅग्रिगेटर आहे: तुम्ही ब्रोकरेज खाते SnapTrade ला एकदा जोडता, आणि SnapTrade ते तुमच्यासाठी वाचते. स्वतःचा सार्वजनिक API नसलेले अनेक ब्रोकर यात समाविष्ट आहेत. Capital **SnapTrade Personal** वापरते, म्हणजे तुमच्या स्वतःच्या खात्यांसाठीची मोफत योजना, तुमच्या स्वतःच्या क्लायंट id आणि कन्झ्युमर कीसह. या योजनेतील डेटा SnapTrade साधारण दिवसातून एकदा रिफ्रेश करते.

काय पाठवले जाते: तुमचा क्लायंट id, आणि स्वाक्षरी म्हणून कन्झ्युमर कीचे स्वतःचे काहीही नाही (विनंत्यांवर त्याने स्वाक्षरी केली जाते). तुमच्या ब्रोकरशी कनेक्शन Capital नव्हे तर SnapTrade ठेवते; त्या कनेक्शनला त्याच्या अटी आणि गोपनीयता धोरण लागू होतात.

### 1. API की तयार करा

1. [SnapTrade dashboard](https://dashboard.snaptrade.com/signup) वर साइन अप करा आणि **Personal** योजना निवडा.
2. डॅशबोर्डमध्ये API की तयार करा. **क्लायंट id** आणि **कन्झ्युमर की** कॉपी करा; कन्झ्युमर की एकदाच दाखवली जाते.

### 2. Capital मध्ये जोडा

1. **ब्रोकर्स → क्रेडेन्शियल्स → क्लायंट id: SnapTrade** आणि **कन्झ्युमर की: SnapTrade**, प्रत्येक मूल्य पेस्ट करा.
2. **ब्रोकर्स → +**: नाव टाका आणि **ब्रोकर** ला SnapTrade सेट करा.
3. **SnapTrade द्वारे ब्रोकरेज जोडा** दाबा. SnapTrade Connection Portal ब्राउझरमध्ये उघडते; तिथे तुमच्या ब्रोकरमध्ये लॉग इन करा (लिंक 5 मिनिटे वैध असते). Capital मध्ये परत या.
4. **खाती मिळवा** दाबा आणि खाते निवडा; त्याचा id **SnapTrade खाते id** फील्डमध्ये भरला जातो. सेव्ह करा.
5. कप्पा उघडा, **होल्डिंग जोडा**, **ट्रॅकिंग** ला **ब्रोकर खाते** सेट करा, खाते निवडा आणि सेव्ह करा, मग **रिफ्रेश करा**.

ज्या खात्याचे सिंक SnapTrade ने अजून पूर्ण केलेले नाही ते *SnapTrade कडे या खात्याचे एकूण मूल्य अजून नाही* असे कळवते; नंतर पुन्हा रिफ्रेश करा.

SnapTrade दस्तऐवज: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Capital मध्ये खाते जोडणे {#connect}

खालील विभागांत प्रत्येक ब्रोकरकडे क्रेडेन्शियल कसे तयार करायचे ते सांगितले आहे. Capital मध्ये सर्वांसाठी पायऱ्या सारख्याच आहेत:

1. **ब्रोकर्स → क्रेडेन्शियल्स**: ब्रोकरची क्रेडेन्शियल बटणे दाबा आणि प्रत्येक मूल्य पेस्ट करा.
2. **ब्रोकर्स → +**: नाव टाका, **ब्रोकर** निवडा, मग **खाती मिळवा** दाबा आणि खाते निवडा (किंवा त्याचा id टाका) आणि सेव्ह करा.
3. कप्पा उघडा, **होल्डिंग जोडा**, **ट्रॅकिंग** ला **ब्रोकर खाते** सेट करा, खाते निवडा आणि सेव्ह करा. **रिफ्रेश करा** दाबा.

## Alpaca {#alpaca}

Alpaca प्रत्येक खात्यासाठी एक की id आणि एक सिक्रेट देते; ती पुन्हा तयार करेपर्यंत वैध राहतात. फक्त लाइव्ह खाती वाचली जातात: पेपर खात्याच्या की लाइव्ह API वर चालत नाहीत.

1. [Alpaca dashboard](https://app.alpaca.markets) मध्ये लॉग इन करा, तुमच्या लाइव्ह खात्यावर जा आणि मुख्य पानावर **API Keys** खाली **Generate New Keys** दाबा.
2. **API Key ID** आणि **Secret Key** कॉपी करा; सिक्रेट एकदाच दाखवले जाते.
3. Capital मध्ये ती **API की: Alpaca** आणि **API सिक्रेट: Alpaca** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा. **खाती मिळवा** कीचा खाते क्रमांक दाखवते.

Alpaca दस्तऐवज: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

Tradier सेटिंग्जमधील API टोकन कधीही कालबाह्य होत नाही.

1. Tradier मध्ये लॉग इन करा आणि [Settings → API Access](https://web.tradier.com/user/api) उघडा. तुमच्या ब्रोकरेज खात्याचा **API Access Token** कॉपी करा (सँडबॉक्स टोकन नव्हे).
2. Capital मध्ये तो **अॅक्सेस टोकन: Tradier** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा.

Tradier दस्तऐवज: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade वैयक्तिक OAuth ग्रँट वापरते: तुम्ही स्वतःसाठी एक अॅप्लिकेशन आणि एक ग्रँट तयार करता, ज्याचा रिफ्रेश टोकन कधीही कालबाह्य होत नाही. Capital प्रत्येक रिफ्रेशला त्याच्या बदल्यात 15 मिनिटांचा अॅक्सेस टोकन मिळवते.

1. [my.tastytrade.com](https://my.tastytrade.com) वर **Manage → My Profile → API → OAuth Applications** उघडा आणि **+ New OAuth client** दाबा. नाव द्या, कोणताही HTTPS redirect URI (उदाहरणार्थ `https://capital.fimych.dev`) आणि फक्त **read** स्कोप निवडा. सेव्ह करा आणि **Client Secret** कॉपी करा; तो एकदाच दाखवला जातो.
2. अॅप्लिकेशनच्या शेजारी **Manage** दाबा, मग **Create Grant**, आणि **refresh token** कॉपी करा.
3. Capital मध्ये ते **रिफ्रेश टोकन: tastytrade** आणि **क्लायंट सिक्रेट: tastytrade** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा.

tastytrade दस्तऐवज: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Public चा Individual API तुमच्या स्वतःच्या खात्यांसाठी आहे. सिक्रेट की दीर्घायुषी आणि रद्द करता येणारी आहे; Capital प्रत्येक रिफ्रेशला तिच्या बदल्यात पाच मिनिटांचा अॅक्सेस टोकन मिळवते.

1. Public च्या वेब अॅपमध्ये सेटिंग्जमधील **API** पान उघडा आणि **secret key** तयार करा.
2. Capital मध्ये ती **सिक्रेट की: Public.com** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा.

Public दस्तऐवज: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

eToro च्या की दीर्घायुषी आहेत; त्यांना समाप्ती तारीख आणि IP यादी देता येते, आणि त्या फक्त-वाचन करता येतात. तुमचे eToro खाते सत्यापित (verified) असले पाहिजे.

1. eToro मध्ये **Settings → Trading → API Key Management** उघडा आणि **Create New Key** दाबा. **Real** वातावरण, **Read** परवानगी, IP यादी नाही, आणि हवी असल्यास समाप्ती तारीख निवडा. SMS कोडने पुष्टी करा.
2. **Public API Key** आणि **User Key** कॉपी करा; user key एकदाच दाखवली जाते.
3. Capital मध्ये त्या **सार्वजनिक API की: eToro** आणि **वापरकर्ता की: eToro** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा. **खाती मिळवा** तुमची ट्रेडिंग, रोख आणि इतर eToro खाती दाखवते.

eToro दस्तऐवज: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Indexa च्या खाजगी विभागातील टोकन फक्त-वाचन आहे. तो तुमचा ई-मेल, पासवर्ड आणि डिव्हाइस यांच्याशी जोडलेला असतो: पासवर्ड बदलल्यावर तो पुन्हा तयार करा.

1. Indexa च्या खाजगी विभागात **वापरकर्ता सेटिंग्ज → अॅप्लिकेशन्स** उघडा आणि टोकन कॉपी करा.
2. Capital मध्ये तो **अॅक्सेस टोकन: Indexa Capital** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा. पेन्शन आणि गुंतवणूक दोन्ही खाती दाखवली जातात.

Indexa फंडांचे मूल्यांकन प्रत्येक व्यावसायिक दिवशी एकदा करते; निरीक्षण तारीख ही ती मूल्यांकन तारीख असते.

Indexa Capital दस्तऐवज: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Bank चा T-Invest API तुम्ही गुंतवणूक सेटिंग्जमध्ये दिलेला टोकन स्वीकारतो. टोकन शेवटच्या वापरानंतर तीन महिन्यांनी निष्क्रिय होतो आणि दिल्यानंतर सात दिवसांत वापरला पाहिजे; आठवड्याला एकदा रिफ्रेश केल्यास तो चालू राहतो. **फक्त-वाचन** टोकन निवडा.

1. [T-Invest सेटिंग्ज](https://www.tbank.ru/invest/settings/) उघडा आणि एक्स्चेंजसाठी **फक्त-वाचन** प्रवेशासह (सर्व खाती किंवा एक) **T-Invest API टोकन** द्या. तो देण्यासाठी कोडने ट्रेडची पुष्टी बंद असली पाहिजे. टोकन कॉपी करा; तो एकदाच दाखवला जातो.
2. Capital मध्ये तो **अॅक्सेस टोकन: T-Invest** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा.

T-Bank हा API रशियन Trusted Root CA अंतर्गत चालवते, जे Android मध्ये समाविष्ट नाही. Capital ते प्रमाणपत्र फक्त T-Invest API च्या पत्त्यासाठी (`invest-public-api.tbank.ru`) विश्वासार्ह मानते, इतर कोणत्याही कनेक्शनसाठी नाही.

T-Invest दस्तऐवज: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR एक वर्ष वैध असलेला रिफ्रेश टोकन देते; Capital प्रत्येक रिफ्रेशला त्याच्या बदल्यात 30 मिनिटांचा अॅक्सेस टोकन मिळवते. ALOR फक्त-वाचन टोकन देत नाही: टोकनने ट्रेड करता येईल, Capital फक्त वाचते.

1. [ALOR developer portal](https://alor.dev) वर साइन इन करा, तुमचे ट्रेडिंग खाते जोडा, **API Access Tokens** उघडा आणि **Create Token** दाबा. रिफ्रेश टोकन कॉपी करा.
2. Capital मध्ये तो **रिफ्रेश टोकन: ALOR** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा. **खाती मिळवा** खात्याचे पोर्टफोलिओ दाखवते (शेअर बाजार D…, चलन बाजार G…, डेरिव्हेटिव्ह्ज 7500…); प्रत्येक पोर्टफोलिओसाठी एक जोडा.

ALOR दस्तऐवज: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Capital.com च्या की डीफॉल्टनुसार एक वर्ष वैध असतात, किंवा तुम्ही निवडलेल्या तारखेपर्यंत. त्यांना ट्रेडिंगचे अधिकार असतात (Capital.com कडे फक्त-वाचन की नाहीत); Capital फक्त वाचते. कीचा स्वतःचा पासवर्ड असतो, जो तुमच्या खात्याचा पासवर्ड नसतो.

1. दोन-घटक प्रमाणीकरण चालू करा, मग **Settings → API integrations** उघडा आणि **Generate API key** दाबा. लेबल आणि **custom password** द्या, समाप्ती तारीख तशीच ठेवा किंवा निवडा, आणि 2FA कोडने पुष्टी करा. की कॉपी करा; ती एकदाच दाखवली जाते.
2. Capital मध्ये **API की: Capital.com**, तुमचा लॉगिन ई-मेल **लॉगिन ई-मेल: Capital.com** म्हणून आणि custom password **API की पासवर्ड: Capital.com** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा. फक्त लाइव्ह खाती वाचली जातात.

Capital.com दस्तऐवज: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) न्यूझीलंडच्या बँका, गुंतवणूक प्लॅटफॉर्म आणि KiwiSaver योजना जोडते; मोफत वैयक्तिक अॅप तुमची स्वतःची खाती वाचते. Akahu डेटा साधारण दिवसातून एकदा रिफ्रेश करते.

1. [my.akahu.nz](https://my.akahu.nz) वर साइन अप करा आणि तुमचे प्रदाते जोडा (उदाहरणार्थ Sharesies, Hatch, Kernel, Simplicity, Milford किंवा तुमची KiwiSaver योजना).
2. **Developers** पान उघडा, डेव्हलपर अटी स्वीकारा आणि **App ID Token** व **User Access Token** कॉपी करा.
3. Capital मध्ये ते **App ID टोकन: Akahu** आणि **वापरकर्ता अॅक्सेस टोकन: Akahu** म्हणून पेस्ट करा, मग [Capital मध्ये खाते जोडणे](#connect) चे अनुसरण करा.

Akahu दस्तऐवज: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## बाजारानुसार लोकप्रिय ब्रोकर {#by-market}

Capital च्या भाषांच्या बाजारांतील सर्वाधिक वापरले जाणारे ब्रोकर कसे जोडता येतात, ऑक्टोबर 2026 नुसार. *थेट* म्हणजे वरील विभाग; *SnapTrade* म्हणजे [SnapTrade](#snaptrade) द्वारे; अन्यथा ते वाचता न येण्याचे कारण, आणि शिल्लक **मॅन्युअल** होल्डिंग म्हणून ठेवता येते.

| बाजार | ब्रोकर | कसे |
|---|---|---|
| अमेरिका | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | थेट |
| अमेरिका | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| अमेरिका | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | सार्वजनिक API नाही |
| कॅनडा | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| कॅनडा | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | सार्वजनिक API नाही |
| युनायटेड किंग्डम आणि आयर्लंड | Trading 212, eToro, Interactive Brokers | थेट |
| युनायटेड किंग्डम आणि आयर्लंड | AJ Bell | SnapTrade |
| युनायटेड किंग्डम आणि आयर्लंड | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | सार्वजनिक API नाही |
| युनायटेड किंग्डम आणि आयर्लंड | IG | शक्य नाही: प्रत्येक सत्राला खात्याचा पासवर्ड लागतो |
| युरोप | Indexa Capital (स्पेन), eToro, Trading 212, Interactive Brokers | थेट |
| युरोप | DEGIRO, BUX | SnapTrade |
| युरोप | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | गुंतवणुकीसाठी सार्वजनिक API नाही |
| युरोप | XTB | शक्य नाही: API मार्च 2025 मध्ये बंद झाला |
| युरोप | Saxo, comdirect | शक्य नाही: फक्त अल्पायुषी टोकन किंवा TAN सत्रे |
| युरोप | Bitpanda, Freedom24 | शक्य नाही: API खात्याचे एकूण मूल्य देत नाही |
| रशिया आणि कझाकस्तान | T-Invest, ALOR | थेट |
| रशिया आणि कझाकस्तान | BCS | शक्य नाही: एकूण मूल्य नाही, आणि त्याचा टोकन 90 दिवसांनी कालबाह्य होतो |
| रशिया आणि कझाकस्तान | Finam | अजून नाही: खात्याच्या मूल्याचे चलन दस्तऐवजात दिलेले नाही |
| रशिया आणि कझाकस्तान | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | सार्वजनिक API नाही, किंवा त्यात एकूण मूल्य नाही |
| भारत | Zerodha, Upstox | SnapTrade (SEBI नियमांनुसार API सत्रे रोज संपतात, म्हणून कनेक्शन वारंवार नूतनीकरण करावे लागते) |
| भारत | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | शक्य नाही: SEBI नियमांनुसार प्रत्येक API सत्र रोज संपते |
| पाकिस्तान आणि बांगलादेश | सर्व एक्स्चेंज ब्रोकर | सार्वजनिक API नाही |
| चीन, हाँगकाँग आणि तैवान | moomoo | SnapTrade |
| चीन, हाँगकाँग आणि तैवान | Futu, Tiger Brokers, Longbridge | अजून नाही: कीचा कालावधी किंवा प्रतिसादाचे स्वरूप पूर्ण दस्तऐवजित नाही, किंवा की फक्त वाचनापुरत्या मर्यादित करता येत नाहीत |
| चीन, हाँगकाँग आणि तैवान | East Money, Huatai, CITIC, Yuanta, Fubon | सार्वजनिक वेब API नाही (फक्त डेस्कटॉप टर्मिनल किंवा प्रमाणपत्र SDK) |
| जपान | OANDA Japan (API प्रवेशासाठी पात्र खाती) | थेट, OANDA प्रमाणे |
| जपान | SBI Securities, Rakuten Securities, Monex, Matsui | सार्वजनिक API नाही |
| ऑस्ट्रेलिया आणि न्यूझीलंड | CommSec, Stake | SnapTrade |
| ऑस्ट्रेलिया आणि न्यूझीलंड | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver schemes | Akahu (न्यूझीलंडची खाती) |
| मध्यपूर्व आणि आफ्रिका | eToro | थेट |
| मध्यपूर्व आणि आफ्रिका | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | व्यक्तींसाठी सार्वजनिक API नाही |
| आग्नेय आशिया | Stockbit, Ajaib, Bibit, IPOT, VPS | सार्वजनिक API नाही |
| आग्नेय आशिया | SSI, TCBS, DNSE | शक्य नाही: एकदाच वापरायच्या कोडसह 8 तासांचे टोकन, किंवा फक्त रोख शिल्लक |
| लॅटिन अमेरिका | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | व्यक्तींसाठी सार्वजनिक API नाही, किंवा फक्त पासवर्डने लॉगिन |
| फॉरेक्स आणि CFD | OANDA, Capital.com | थेट |
| फॉरेक्स आणि CFD | MetaTrader ब्रोकर (XM, Exness, Pepperstone, IC Markets, Admirals) | शक्य नाही: HTTPS वाचन प्रवेश नाही |
| फॉरेक्स आणि CFD | cTrader ब्रोकर, FXCM, Forex.com | शक्य नाही: अॅप नोंदणी, कालबाह्य API किंवा पासवर्डने लॉगिन |

## इतर ब्रोकर {#other-brokers}

Capital फक्त अशा इंटरफेसशी जोडते जे फोनवरून HTTPS द्वारे चालतात, ज्यासाठी तुम्ही स्वतः टोकन तयार करू शकता आणि जे ट्रेड करू न देता फक्त वाचू देतात. त्यामुळे सध्या हे वगळले जाते:

- **MetaTrader 4 आणि 5** खाती. इन्व्हेस्टर पासवर्ड फक्त-वाचन प्रवेश देतो, पण फक्त MetaTrader टर्मिनलच्या आत; ब्रोकर त्यासाठी HTTPS इंटरफेस प्रकाशित करत नाहीत.
- ज्या ब्रोकरच्या API ला संगणकावर चालणारा प्रोग्राम लागतो (उदाहरणार्थ Interactive Brokers Client Portal Web API गेटवे; Capital त्याऐवजी Flex Web Service वापरते) किंवा OAuth अॅप्लिकेशन नोंदणी लागते.
- ज्या ब्रोकरचा API OAuth द्वारे फक्त अल्पायुषी टोकन देतो, उदाहरणार्थ Saxo Bank (अॅक्सेस टोकन 20 मिनिटे टिकतात; डेव्हलपर पोर्टलचा 24-तासांचा टोकन फक्त सिम्युलेशन वातावरणासाठी चालतो).
- सार्वजनिक API नसलेल्या बँका आणि ब्रोकर.

यांपैकी अनेक ब्रोकर [SnapTrade](#snaptrade) मध्ये समाविष्ट आहेत. अन्यथा ती शिल्लक **मॅन्युअल** होल्डिंग म्हणून टाका आणि स्टेटमेंट तपासताना आकडा अपडेट करा. तुमचा ब्रोकर खात्याचे मूल्य वाचणारा साधा टोकन-आधारित HTTPS एंडपॉइंट देत असेल, तर त्याच्या दस्तऐवजाच्या लिंकसह [इश्यू उघडा]({{ site.repo }}/issues). Capital मधील प्रत्येक ब्रोकर हा एक छोटा प्लगइन आहे; डेव्हलपर [प्लगइन मार्गदर्शकाचे]({{ site.repo }}/blob/main/BROKER-PLUGINS.md) अनुसरण करून एक जोडू शकतात.

## संदेश आणि काय करावे {#messages}

| संदेश | काय करावे |
|---|---|
| *Interactive Brokers साठी ब्रोकर्स स्क्रीनवर क्रेडेन्शियल्स हवी आहेत* / *OANDA साठी … क्रेडेन्शियल्स हवी आहेत* | टोकन, की किंवा क्लायंट id ब्रोकर्स टॅबवरील क्रेडेन्शियल्स मध्ये पेस्ट करा. |
| *टोकनची मुदत संपली आहे; Client Portal मध्ये नवीन टोकन तयार करा* | नवीन Flex Web Service टोकन तयार करा आणि पेस्ट करा. |
| *टोकन अवैध आहे* | टोकन पुन्हा कॉपी करा; नवीन टोकन जुन्याची जागा घेतो. |
| *टोकन दुसऱ्या IP पत्त्यापुरते मर्यादित आहे* | IP मर्यादेशिवाय टोकन तयार करा. |
| *Flex Query id अवैध आहे* | संख्या तपासा; क्वेरी या लॉगिनची Activity Flex Query असली पाहिजे. |
| *Flex Query मध्ये Report Date आणि Total सह Net Asset Value (NAV) Summary in Base हा विभाग जोडा* | क्वेरी संपादित करा आणि विभाग व फील्ड जोडा. |
| *Flex Query मध्ये Account Information मधील Currency फील्ड जोडा* | क्वेरी संपादित करा आणि फील्ड जोडा. |
| *क्वेरीने N खाती परत केली; प्रत्येक खात्यासाठी स्वतंत्र Flex Query तयार करा* | एकच खाते असलेली क्वेरी तयार करा. |
| *स्टेटमेंट अजून तयार नाही; एका मिनिटाने पुन्हा रिफ्रेश करा* | Interactive Brokers अजून रिपोर्ट तयार करत आहे; पुन्हा रिफ्रेश करा. |
| *प्रवेश नाकारला; प्रदात्याची की किंवा कोटा तपासा* | OANDA टोकन चुकीचा आहे किंवा रद्द केला गेला आहे, किंवा Trading 212 की-जोडी चुकीची आहे किंवा तिच्याकडे Account data परवानगी नाही. |
| *SnapTrade कडे या खात्याचे एकूण मूल्य अजून नाही; कनेक्शन सिंक करा आणि पुन्हा प्रयत्न करा* | SnapTrade ने ब्रोकरेज अजून सिंक केलेले नाही; नंतर पुन्हा रिफ्रेश करा. |
| *अजून कोणतेही खाते जोडलेले नाही. आधी SnapTrade द्वारे ब्रोकरेज जोडा.* | एडिटरमधून Connection Portal उघडा आणि ब्रोकर जोडा. |
| *खात्याचे ऋण मूल्य … समर्थित नाही* | खाते डेबिटमध्ये आहे; ते तुमच्या बचतीत काहीही भर घालत नाही. |
| *tastytrade ने रिफ्रेश टोकन किंवा क्लायंट सिक्रेट नाकारला; नवीन ग्रँट तयार करा* | अॅप्लिकेशनसाठी नवीन ग्रँट तयार करा आणि त्याचा रिफ्रेश टोकन पेस्ट करा; क्लायंट सिक्रेट तपासा. |
| *Capital.com ने सेशन उघडले नाही; API की, लॉगिन आणि की पासवर्ड तपासा* | की, ई-मेल किंवा कीचा custom पासवर्ड चुकीचा आहे, किंवा की कालबाह्य झाली आहे. |
| *खाते सापडले नाही; ते पुन्हा निवडा* | ब्रोकर आता हे खाते दाखवत नाही; ते संपादित करा आणि **खाती मिळवा** मधून निवडा. |

यांपैकी कोणतेही घडल्यावर मागील मूल्य दिसत राहते, जुने म्हणून चिन्हांकित.
