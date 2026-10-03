---
layout: default
lang: ar
base: "/ar"
key: "accounts"
title: حسابات الوساطة والفوركس
class: doc
---
# حسابات الوساطة والفوركس

يستطيع Capital قراءة القيمة الإجمالية لحساب وساطة أو فوركس بالطريقة نفسها التي يقرأ بها المحفظة الرقمية. تضيف الحساب إلى صندوق بوصفه أصلًا من نوع **حساب وساطة**، ويجلب كل تحديث صافي قيمة أصول الحساب بعملته الأساسية. التطبيق يقرأ فقط: يستخدم واجهة التقارير الخاصة بالوسيط برمز تنشئه بنفسك، ولا ينفّذ أي أمر ولا يعدّله ولا يلغيه أبدًا، ولا ينقل الأموال.

لا يتصل Capital إلا بالواجهات التي تكون بيانات اعتمادها طويلة الأمد: رمز أو مفتاح تنشئه مرة واحدة ويبقى صالحًا حتى تلغيه، أو حتى انتهاء مدة اخترتها، أو لأشهر على الأقل (تنقضي رموز T-Invest بعد ثلاثة أشهر دون استخدام، ورموز ALOR بعد سنة). كل وسيط مما يلي متاح أيًّا كانت لغة التطبيق. المدعوم حاليًا:

| الوسيط | الواجهة المستخدمة | ما يُقرأ |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (لاسترجاع التقارير فقط) | صافي قيمة الأصول في آخر يوم عمل، بالعملة الأساسية للحساب |
| [OANDA](#oanda) | واجهة v20 REST API، حسابات fxTrade الحقيقية | صافي قيمة الأصول وقت التحديث، بعملة الحساب |
| [Trading 212](#trading-212) | واجهة Public API، حسابات Invest وStocks ISA | القيمة الإجمالية للحساب وقت التحديث، بالعملة الأساسية للحساب |
| [SnapTrade](#snaptrade) | SnapTrade Personal، وهي منصة مُجمِّع (aggregator) تغطي وسطاء كثرًا | القيمة الإجمالية للحساب كما يبلّغ بها الوسيط إلى SnapTrade، بعملة الحساب |
| [Alpaca](#alpaca) | واجهة Trading API، الحسابات الحقيقية | حقوق الملكية (النقد زائد المراكز)، بالدولار الأمريكي |
| [Tradier](#tradier) | واجهة Brokerage API | إجمالي حقوق الملكية، بالدولار الأمريكي |
| [tastytrade](#tastytrade) | واجهة Open API بتفويض OAuth شخصي | صافي قيمة التصفية، بالدولار الأمريكي |
| [Public.com](#public) | واجهة Individual API | القيمة الإجمالية للحساب، بالدولار الأمريكي |
| [eToro](#etoro) | واجهة Public API | رصيد الحساب المختار (في حساب التداول: النقد زائد المراكز المستثمرة)، بعملته |
| [Indexa Capital](#indexa-capital) | واجهة REST API، رمز للقراءة فقط | إجمالي المحفظة في آخر تاريخ تقييم، بعملة الحساب |
| [T-Invest](#t-invest) | واجهة T-Invest API (T-Bank) | القيمة الإجمالية للمحفظة، بالروبل |
| [ALOR](#alor) | واجهة ALOR OpenAPI | تقييم المحفظة في بورصة موسكو، بالروبل |
| [Capital.com](#capital-com) | واجهة Public API، الحسابات الحقيقية | الرصيد شاملًا الربح والخسارة المفتوحين، بعملة الحساب |
| [Akahu](#akahu) | تطبيق Akahu شخصي، وهي منصة مُجمِّع نيوزيلندية | رصيد حساب مربوط (Sharesies وHatch وKernel وKiwiSaver وغيرها)، بعملته |

## قبل أن تبدأ {#before-you-start}

- **ما الذي يغادر الجهاز.** يرسل التطبيق عند كل تحديث رمز الوصول ومعرّف الحساب أو الاستعلام إلى ذلك الوسيط عبر HTTPS. ويرى الوسيط عنوان IP الخاص بك، كما في أي طلب.
- **أين تُحفظ بيانات الاعتماد.** تبويب الوسطاء → بيانات الاعتماد. تُشفَّر بمفتاح محفوظ في Android Keystore، ولا تُكتب أبدًا في مجلد بياناتك، وتُستثنى من النسخ المُصدَّرة ومن النسخ الاحتياطي للنظام. تكفي مجموعة واحدة من بيانات الاعتماد لكل وسيط لجميع الحسابات التي تضيفها لدى ذلك الوسيط.
- **ما الذي يُخزَّن في مجلدك.** معرّف الحساب، وآخر قيمة قُرئت ووقت قراءتها. ولا شيء غير ذلك من الوسيط.
- **قد تتغير شاشات الوسيط.** الخطوات أدناه تطابق مواقع الوسطاء كما كانت في أكتوبر 2026. يغيّر الوسطاء أسماء القوائم ويحرّكون الإعدادات من حين لآخر، فقد تبدو الخطوة مختلفة قليلًا عند تنفيذها. وثائق الوسيط نفسه، المرتبطة في كل قسم، هي المرجع المعتمد: إن لم تعد خطوة هنا مطابقة، فابحث عن المصطلح نفسه في صفحة الوسيط.

## Interactive Brokers {#interactive-brokers}

يستخدم Capital خدمة **Flex Web Service**، وهي واجهة Interactive Brokers لجلب التقارير المهيَّأة مسبقًا. الرمز الذي تستخدمه لا يستطيع إلا إنشاء التقارير وتنزيلها؛ ولا يستطيع تسجيل الدخول ولا التداول ولا السحب. يطلب Capital تقرير **Net Asset Value (NAV) Summary in Base** من استعلام Activity Flex Query ويأخذ الإجمالي لأحدث تاريخ تقرير، فتكون القيمة هي الإقفال في آخر يوم عمل.

### 1. إنشاء Flex Query

1. سجّل الدخول إلى [Client Portal](https://www.interactivebrokers.com/portal) وافتح **Performance & Reports → Flex Queries** (التقارير والأداء)، وفي بعض الحسابات تسمى القائمة *Reporting*.
2. تحت **Activity Flex Query** اضغط **+** (إنشاء). امنح الاستعلام اسمًا، مثل `Capital`.
3. في قائمة **Sections** فعّل هذين القسمين والحقلين فيهما بالضبط (ويصلح أيضًا تحديد جميع حقول القسم):
   - **Account Information**: *Account ID*، *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*، *Total*.
4. في **Delivery Configuration** اضبط **Format** على `XML` و**Period** على `Last Business Day`. يمكن ترك الخيارات الأخرى على قيمها الافتراضية.
5. احفظ الاستعلام، ثم اضغط أيقونة **i** (معلومات) بجانبه ودوّن **Query ID**، وهو رقم.

يجب أن يغطي الاستعلام حسابًا واحدًا. إن كانت لديك حسابات مرتبطة أو بنية مستشار مالي، فأنشئ استعلامًا لكل حساب وحدّد ذلك الحساب وحده عند إنشائه.

### 2. تفعيل Flex Web Service وإنشاء الرمز

1. في صفحة **Flex Queries** نفسها افتح **Flex Web Service Configuration**.
2. شغّل **Flex Web Service Status** واحفظ. يُنشأ رمز.
3. لاختيار مدة صلاحية الرمز اضغط **Generate New Token**: من 6 ساعات إلى سنة واحدة. اترك **Valid for IP address** فارغًا للهاتف، لأن عنوانه يتغير. إنشاء رمز جديد يبطل الرمز السابق.
4. انسخ الرمز.

### 3. ربطه في Capital

1. **الوسطاء → بيانات الاعتماد → رمز الوصول: Interactive Brokers**، الصق الرمز واحفظ.
2. **الوسطاء → +**: أدخل اسمًا، واضبط **الوسيط** على Interactive Brokers، وأدخل **معرّف Flex Query** واحفظ.
3. افتح الصندوق، **إضافة أصل**، اضبط **التتبع** على **حساب وساطة**، واختر الحساب واحفظ.
4. اضغط **تحديث**. يستغرق التشغيل الأول حتى نصف دقيقة لأن التقرير يُنشأ عند الطلب.

عندما تنتهي صلاحية الرمز يبلّغ التحديث عن *انتهت صلاحية الرمز؛ أنشئ رمزًا جديدًا في Client Portal*: أنشئ رمزًا جديدًا والصقه ضمن بيانات الاعتماد. تسمح Interactive Brokers بطلب تقرير واحد في الثانية وعشرة طلبات في الدقيقة لكل رمز، ولا يتجاوز التحديث ذلك أبدًا.

وثائق Interactive Brokers: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

يستدعي Capital **ملخص الحساب** في واجهة OANDA v20 REST API ويخزّن صافي قيمة أصول الحساب (الرصيد زائد الربح أو الخسارة غير المحققة) بعملة الحساب. لا تُدعم إلا حسابات **fxTrade** الحقيقية؛ فالحسابات التجريبية ليست مدخرات.

**رمز الوصول الشخصي في OANDA ليس للقراءة فقط.** فهو يمنح وصولًا كاملًا إلى API لكل الحسابات الفرعية المرتبطة بتسجيل دخولك، بما في ذلك التداول. لا يستدعي Capital إلا ملخص الحساب، لكن أي شخص يحصل على الرمز يستطيع التداول به. تعامل معه كما تتعامل مع كلمة المرور: الصقه في Capital فقط، وألغِه في بوابة OANDA إذا فقدت الهاتف.

### 1. إنشاء الرمز

1. سجّل الدخول إلى بوابة إدارة حساب OANDA fxTrade.
2. افتح **My Services → Manage API Access** (خدماتي ← إدارة الوصول إلى API)، وفي البوابة الأقدم: *My Account → My Services → Manage API Access*.
3. اقبل ترخيص API واضغط **Generate**. انسخ الرمز؛ فلن تعرضه OANDA مرة أخرى. وإن فقدته فألغِه هناك وأنشئ رمزًا جديدًا.

### 2. العثور على معرّف الحساب

يأتي معرّف حساب v20 بالشكل `001-001-1234567-001`، مع الشرطات. وهو مدرج في البوابة نفسها بجانب كل حساب فرعي، وفي منصة fxTrade ضمن تفاصيل الحساب.

### 3. ربطه في Capital

1. **الوسطاء → بيانات الاعتماد → رمز الوصول: OANDA**، الصق الرمز واحفظ.
2. **الوسطاء → +**: أدخل اسمًا، واضبط **الوسيط** على OANDA، وأدخل **معرّف حساب OANDA** واحفظ.
3. افتح الصندوق، **إضافة أصل**، اضبط **التتبع** على **حساب وساطة**، واختر الحساب واحفظ.
4. اضغط **تحديث**.

يُبلَّغ عن حساب الهامش الذي يكون صافي قيمته سالبًا بوصفه خطأً ولا يُحتسب ضمن المدخرات.

وثائق OANDA: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

يستدعي Capital **ملخص الحساب** في واجهة Trading 212 Public API ويخزّن القيمة الإجمالية للحساب بالعملة الأساسية للحساب. تغطي الواجهة حسابات **Invest** و**Stocks ISA**؛ وزوج المفاتيح يخص حسابًا واحدًا، ويحتفظ Capital بزوج مفاتيح واحد، فيقرأ حساب Trading 212 واحدًا.

### 1. إنشاء مفتاح API

1. في تطبيق Trading 212 أو موقعه افتح القائمة (**☰**) ← **Settings** ← **API (Beta)** ووافق على تحذير المخاطر.
2. اضغط **Generate API key**. امنحه اسمًا، وأبقِ صلاحية **Account data** (قراءة) وحدها، واختر وصولًا *Unrestricted* بالنسبة إلى IP (فعنوان الهاتف يتغير).
3. أرسل النموذج. انسخ القيمتين: **API Key** و**API Secret Key**. يُعرض المفتاح السري مرة واحدة؛ وإن فقدته فاحذف المفتاح وأنشئ زوجًا جديدًا.

### 2. ربطه في Capital

1. **الوسطاء → بيانات الاعتماد → مفتاح API: Trading 212** و**المفتاح السري لـ API: Trading 212**، الصق كل قيمة.
2. **الوسطاء → +**: أدخل اسمًا، واضبط **الوسيط** على Trading 212، وأدخل **رقم حساب Trading 212** (معرّف الحساب الظاهر في التطبيق، أرقام فقط) واحفظ.
3. افتح الصندوق، **إضافة أصل**، اضبط **التتبع** على **حساب وساطة**، واختر الحساب واحفظ.
4. اضغط **تحديث**. تسمح Trading 212 بطلب ملخص واحد كل 5 ثوانٍ.

وثائق Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) مُجمِّع (aggregator): تربط حساب وساطة بـ SnapTrade مرة واحدة، ثم يقرؤه SnapTrade نيابةً عنك. وهو يغطي وسطاء كثرًا لا تتوفر لديهم API عامة خاصة بهم. يستخدم Capital خطة **SnapTrade Personal**، المجانية لحساباتك الشخصية، مع معرّف العميل (client id) ومفتاح المستهلك (consumer key) الخاصين بك. يحدّث SnapTrade البيانات في هذه الخطة نحو مرة في اليوم.

ما يُرسل: معرّف العميل الخاص بك، أما مفتاح المستهلك نفسه فلا يُرسل منه شيء (تُوقَّع الطلبات به). SnapTrade، لا Capital، هو من يحتفظ بالاتصال بوسيطك؛ وتنطبق شروطه وسياسة الخصوصية لديه على ذلك الاتصال.

### 1. إنشاء مفتاح API

1. سجّل في [لوحة SnapTrade](https://dashboard.snaptrade.com/signup) واختر خطة **Personal**.
2. أنشئ مفتاح API في اللوحة. انسخ **معرّف العميل** و**مفتاح المستهلك**؛ يُعرض مفتاح المستهلك مرة واحدة.

### 2. ربطه في Capital

1. **الوسطاء → بيانات الاعتماد → معرّف العميل: SnapTrade** و**مفتاح المستهلك: SnapTrade**، الصق كل قيمة.
2. **الوسطاء → +**: أدخل اسمًا واضبط **الوسيط** على SnapTrade.
3. اضغط **ربط وسيط عبر SnapTrade**. تفتح بوابة SnapTrade Connection Portal في المتصفح؛ سجّل الدخول إلى وسيطك فيها (الرابط صالح 5 دقائق). ثم عد إلى Capital.
4. اضغط **جلب الحسابات** واختر الحساب؛ فيملأ معرّفه حقل **معرّف حساب SnapTrade**. احفظ.
5. افتح الصندوق، **إضافة أصل**، اضبط **التتبع** على **حساب وساطة**، واختر الحساب واحفظ، ثم **تحديث**.

الحساب الذي لم ينتهِ SnapTrade من مزامنته بعد يبلّغ عن *ليست لدى SnapTrade قيمة إجمالية لهذا الحساب بعد*؛ حدّث مرة أخرى لاحقًا.

وثائق SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## ربط حساب في Capital {#connect}

تشرح الأقسام التالية كيف تنشئ بيانات الاعتماد لدى كل وسيط. أما في Capital فالخطوات واحدة للجميع:

1. **الوسطاء → بيانات الاعتماد**: اضغط أزرار بيانات اعتماد الوسيط والصق كل قيمة.
2. **الوسطاء → +**: أدخل اسمًا، واختر **الوسيط**، ثم اضغط **جلب الحسابات** واختر الحساب (أو اكتب معرّفه) واحفظ.
3. افتح صندوقًا، **إضافة أصل**، اضبط **التتبع** على **حساب وساطة**، واختر الحساب واحفظ. اضغط **تحديث**.

## Alpaca {#alpaca}

تصدر Alpaca معرّف مفتاح ورمزًا سريًا لكل حساب؛ ويبقيان صالحين حتى تعيد إنشاءهما. لا تُقرأ إلا الحسابات الحقيقية: مفاتيح الحساب التجريبي (paper) لا تعمل مع الواجهة الحقيقية.

1. سجّل الدخول إلى [لوحة Alpaca](https://app.alpaca.markets)، وانتقل إلى حسابك الحقيقي، وفي الصفحة الرئيسية، تحت **API Keys**، اضغط **Generate New Keys**.
2. انسخ **API Key ID** و**Secret Key**؛ يُعرض المفتاح السري مرة واحدة.
3. في Capital الصقهما في **مفتاح API: Alpaca** و**المفتاح السري لـ API: Alpaca**، ثم اتبع [ربط حساب](#connect). يعرض **جلب الحسابات** رقم حساب المفتاح.

وثائق Alpaca: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

رمز API من إعدادات Tradier لا تنتهي صلاحيته.

1. سجّل الدخول إلى Tradier وافتح [Settings → API Access](https://web.tradier.com/user/api). انسخ **API Access Token** الخاص بحساب الوساطة لديك (لا رمز البيئة التجريبية).
2. في Capital الصقه في **رمز الوصول: Tradier**، ثم اتبع [ربط حساب](#connect).

وثائق Tradier: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

يستخدم tastytrade تفويض OAuth شخصيًا: تنشئ تطبيقًا لنفسك وتفويضًا لا تنتهي صلاحية رمز التحديث فيه. يستبدل Capital به رمز وصول مدته 15 دقيقة عند كل تحديث.

1. في [my.tastytrade.com](https://my.tastytrade.com) افتح **Manage → My Profile → API → OAuth Applications** واضغط **+ New OAuth client**. امنحه اسمًا، وأي عنوان إعادة توجيه HTTPS (مثل `https://capital.fimych.dev`) ونطاق **read** وحده. احفظ وانسخ **Client Secret**؛ يُعرض مرة واحدة.
2. اضغط **Manage** بجانب التطبيق، ثم **Create Grant**، وانسخ **رمز التحديث** (refresh token).
3. في Capital الصقهما في **رمز التحديث: tastytrade** و**سر العميل: tastytrade**، ثم اتبع [ربط حساب](#connect).

وثائق tastytrade: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

واجهة Individual API لدى Public مخصصة لحساباتك أنت. المفتاح السري طويل الأمد ويمكن إلغاؤه؛ ويستبدل Capital به رمز وصول مدته خمس دقائق عند كل تحديث.

1. في تطبيق الويب لدى Public افتح صفحة **API** في إعداداتك وأنشئ **مفتاحًا سريًا** (secret key).
2. في Capital الصقه في **المفتاح السري: Public.com**، ثم اتبع [ربط حساب](#connect).

وثائق Public: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

مفاتيح eToro طويلة الأمد؛ يمكنك منحها تاريخ انتهاء وقائمة عناوين IP، ويمكنك جعلها للقراءة فقط. يجب أن يكون حسابك في eToro موثَّقًا.

1. في eToro افتح **Settings → Trading → API Key Management** واضغط **Create New Key**. اختر البيئة **Real** والصلاحية **Read**، بلا قائمة IP، وتاريخ انتهاء اختياريًا. أكّد برمز SMS.
2. انسخ **Public API Key** و**User Key**؛ يُعرض مفتاح المستخدم مرة واحدة.
3. في Capital الصقهما في **مفتاح API العام: eToro** و**مفتاح المستخدم: eToro**، ثم اتبع [ربط حساب](#connect). يسرد **جلب الحسابات** حسابات التداول والنقد وغيرها في eToro.

وثائق eToro: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

الرمز المأخوذ من المنطقة الخاصة في Indexa للقراءة فقط. وهو مرتبط ببريدك الإلكتروني وكلمة مرورك وجهازك: بعد تغيير كلمة المرور أنشئه من جديد.

1. في المنطقة الخاصة في Indexa افتح **إعدادات المستخدم → التطبيقات** (User settings → Applications) وانسخ الرمز.
2. في Capital الصقه في **رمز الوصول: Indexa Capital**، ثم اتبع [ربط حساب](#connect). تُسرد حسابات التقاعد وحسابات الاستثمار معًا.

تقيّم Indexa الصناديق مرة واحدة في كل يوم عمل؛ وتاريخ الرصد هو تاريخ ذلك التقييم.

وثائق Indexa Capital: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

تقبل واجهة T-Invest API لدى T-Bank رمزًا تصدره في إعدادات الاستثمار. تنقضي صلاحية الرمز بعد ثلاثة أشهر من آخر استخدام، ويجب استخدامه خلال سبعة أيام من إصداره؛ ويُبقيه التحديث الأسبوعي صالحًا. اختر رمزًا **للقراءة فقط**.

1. افتح [إعدادات T-Invest](https://www.tbank.ru/invest/settings/) وأصدر **رمز واجهة T-Invest** (T-Invest API token) للبورصة بصلاحية **القراءة فقط** (لكل الحسابات أو لحساب واحد). يجب أن يكون تأكيد الصفقات بالرمز معطّلًا ليتم الإصدار. انسخ الرمز؛ يُعرض مرة واحدة.
2. في Capital الصقه في **رمز الوصول: T-Invest**، ثم اتبع [ربط حساب](#connect).

تقدّم T-Bank هذه الواجهة بشهادة Russian Trusted Root CA التي لا يتضمنها Android. يثق Capital بتلك الشهادة لعنوان واجهة T-Invest وحده (`invest-public-api.tbank.ru`)، ولا لأي اتصال آخر.

وثائق T-Invest: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

تصدر ALOR رمز تحديث صالحًا لسنة واحدة؛ ويستبدل Capital به رمز وصول مدته 30 دقيقة عند كل تحديث. لا توفر ALOR رمزًا للقراءة فقط: الرمز يستطيع التداول، أما Capital فيقرأ فقط.

1. سجّل الدخول إلى [بوابة مطوري ALOR](https://alor.dev)، واربط حساب التداول لديك، وافتح **API Access Tokens** واضغط **Create Token**. انسخ رمز التحديث.
2. في Capital الصقه في **رمز التحديث: ALOR**، ثم اتبع [ربط حساب](#connect). يسرد **جلب الحسابات** محافظ الحساب (سوق الأسهم D…، وسوق العملات G…، والمشتقات 7500…)؛ أضف حسابًا لكل محفظة.

وثائق ALOR: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

مفاتيح Capital.com صالحة لسنة واحدة افتراضيًا، أو حتى التاريخ الذي تختاره. وهي تحمل صلاحيات التداول (ليست لدى Capital.com مفاتيح للقراءة فقط)؛ أما Capital فيقرأ فقط. للمفتاح كلمة مرور خاصة به، وهي ليست كلمة مرور حسابك.

1. فعّل المصادقة الثنائية، ثم افتح **Settings → API integrations** واضغط **Generate API key**. امنحه تسمية و**كلمة مرور مخصصة**، وأبقِ مدة الانتهاء أو عدّلها، وأكّد برمز المصادقة الثنائية. انسخ المفتاح؛ يُعرض مرة واحدة.
2. في Capital الصق **مفتاح API: Capital.com**، وبريدك الإلكتروني لتسجيل الدخول في **البريد الإلكتروني لتسجيل الدخول: Capital.com**، وكلمة المرور المخصصة في **كلمة مرور مفتاح API: Capital.com**، ثم اتبع [ربط حساب](#connect). لا تُقرأ إلا الحسابات الحقيقية.

وثائق Capital.com: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

يربط [Akahu](https://www.akahu.nz) المصارف ومنصات الاستثمار وخطط KiwiSaver في نيوزيلندا؛ ويقرأ تطبيق شخصي مجاني حساباتك أنت. يحدّث Akahu البيانات نحو مرة في اليوم.

1. سجّل في [my.akahu.nz](https://my.akahu.nz) واربط مزوّديك (مثل Sharesies وHatch وKernel وSimplicity وMilford أو خطة KiwiSaver لديك).
2. افتح صفحة **Developers**، واقبل شروط المطورين وانسخ **App ID Token** و**User Access Token**.
3. في Capital الصقهما في **رمز معرّف التطبيق: Akahu** و**رمز وصول المستخدم: Akahu**، ثم اتبع [ربط حساب](#connect).

وثائق Akahu: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## أشهر الوسطاء حسب السوق {#by-market}

كيف يمكن ربط أكثر الوسطاء استخدامًا في أسواق لغات Capital، حتى أكتوبر 2026. تعني *مباشر* قسمًا مما سبق؛ وتعني *SnapTrade* الربط عبر [SnapTrade](#snaptrade)؛ وفيما عدا ذلك يُذكر سبب تعذّر القراءة، ويمكن إبقاء الرصيد أصلًا **يدويًا**.

| السوق | الوسيط | الطريقة |
|---|---|---|
| الولايات المتحدة | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | مباشر |
| الولايات المتحدة | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| الولايات المتحدة | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | لا توجد API عامة |
| كندا | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| كندا | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | لا توجد API عامة |
| المملكة المتحدة وأيرلندا | Trading 212, eToro, Interactive Brokers | مباشر |
| المملكة المتحدة وأيرلندا | AJ Bell | SnapTrade |
| المملكة المتحدة وأيرلندا | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | لا توجد API عامة |
| المملكة المتحدة وأيرلندا | IG | غير ممكن: كل جلسة تتطلب كلمة مرور الحساب |
| أوروبا | Indexa Capital (إسبانيا), eToro, Trading 212, Interactive Brokers | مباشر |
| أوروبا | DEGIRO, BUX | SnapTrade |
| أوروبا | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | لا توجد API عامة للاستثمارات |
| أوروبا | XTB | غير ممكن: أُغلقت الواجهة في مارس 2025 |
| أوروبا | Saxo, comdirect | غير ممكن: رموز قصيرة العمر أو جلسات TAN فقط |
| أوروبا | Bitpanda, Freedom24 | غير ممكن: لا تعيد الواجهة القيمة الإجمالية للحساب |
| روسيا وكازاخستان | T-Invest, ALOR | مباشر |
| روسيا وكازاخستان | BCS | غير ممكن: لا قيمة إجمالية، وينتهي رمزها بعد 90 يومًا |
| روسيا وكازاخستان | Finam | ليس بعد: عملة قيمة الحساب غير موثّقة |
| روسيا وكازاخستان | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | لا توجد API عامة، أو لا قيمة إجمالية فيها |
| الهند | Zerodha, Upstox | SnapTrade (تنهي قواعد SEBI جلسات API يوميًا، فيحتاج الاتصال إلى تجديد متكرر) |
| الهند | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | غير ممكن: تنهي قواعد SEBI كل جلسة API يوميًا |
| باكستان وبنغلاديش | جميع وسطاء البورصة | لا توجد API عامة |
| الصين وهونغ كونغ وتايوان | moomoo | SnapTrade |
| الصين وهونغ كونغ وتايوان | Futu, Tiger Brokers, Longbridge | ليس بعد: مدة صلاحية المفتاح أو صيغة الاستجابة غير موثّقة بالكامل، أو لا يمكن حصر المفاتيح في القراءة |
| الصين وهونغ كونغ وتايوان | East Money, Huatai, CITIC, Yuanta, Fubon | لا توجد API ويب عامة (منصات سطح المكتب أو حزم SDK بشهادات فقط) |
| اليابان | OANDA Japan (الحسابات المؤهلة للوصول إلى API) | مباشر، مثل OANDA |
| اليابان | SBI Securities, Rakuten Securities, Monex, Matsui | لا توجد API عامة |
| أستراليا ونيوزيلندا | CommSec, Stake | SnapTrade |
| أستراليا ونيوزيلندا | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver schemes | Akahu (حسابات نيوزيلندا) |
| الشرق الأوسط وأفريقيا | eToro | مباشر |
| الشرق الأوسط وأفريقيا | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | لا توجد API عامة للأفراد |
| جنوب شرق آسيا | Stockbit, Ajaib, Bibit, IPOT, VPS | لا توجد API عامة |
| جنوب شرق آسيا | SSI, TCBS, DNSE | غير ممكن: رموز مدتها 8 ساعات برمز لمرة واحدة، أو الرصيد النقدي فقط |
| أمريكا اللاتينية | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | لا توجد API عامة للأفراد، أو تسجيل دخول بكلمة مرور فقط |
| الفوركس وعقود الفروقات | OANDA, Capital.com | مباشر |
| الفوركس وعقود الفروقات | وسطاء MetaTrader (XM, Exness, Pepperstone, IC Markets, Admirals) | غير ممكن: لا وصول للقراءة عبر HTTPS |
| الفوركس وعقود الفروقات | وسطاء cTrader, FXCM, Forex.com | غير ممكن: تسجيل تطبيق، أو واجهة متقادمة، أو تسجيل دخول بكلمة مرور |

## وسطاء آخرون {#other-brokers}

لا يتصل Capital إلا بالواجهات التي تعمل من الهاتف عبر HTTPS برمز تستطيع إنشاءه بنفسك وتقرأ دون أن تتيح التداول. وهذا يستبعد حاليًا:

- حسابات **MetaTrader 4 و5**. كلمة مرور المستثمر (investor) تمنح وصولًا للقراءة فقط، لكن داخل منصة MetaTrader وحدها؛ ولا ينشر الوسطاء واجهة HTTPS لذلك.
- الوسطاء الذين تتطلب واجهة API لديهم برنامجًا يعمل على حاسوب (مثل بوابة Interactive Brokers Client Portal Web API؛ ويستخدم Capital بدلًا منها Flex Web Service) أو تسجيل تطبيق OAuth.
- الوسطاء الذين تصدر واجهة API لديهم رموزًا قصيرة العمر فقط عبر OAuth، مثل Saxo Bank (تدوم رموز الوصول 20 دقيقة؛ ورمز 24 ساعة في بوابة المطورين يغطي البيئة التجريبية فقط).
- البنوك والوسطاء الذين لا يملكون API عامة.

يغطي [SnapTrade](#snaptrade) كثيرًا من هؤلاء الوسطاء. وإلا فأدخل الرصيد بوصفه أصلًا **يدوي** وحدّث الرقم عندما تراجع كشف حسابك. وإن كان وسيطك يوفّر نقطة وصول HTTPS بسيطة تعتمد على رمز وتقرأ قيمة الحساب، فـ[افتح بلاغًا]({{ site.repo }}/issues) مع رابط وثائقها. كل وسيط في Capital إضافة (plugin) صغيرة؛ ويمكن للمطورين إضافة وسيط جديد باتباع [دليل الإضافات]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## الرسائل وما يجب فعله {#messages}

| الرسالة | ما يجب فعله |
|---|---|
| *يحتاج Interactive Brokers إلى بيانات اعتماده في شاشة الوسطاء* / *يحتاج OANDA إلى بيانات اعتماده …* | الصق الرمز أو المفتاح أو معرّف العميل ضمن بيانات الاعتماد في تبويب الوسطاء. |
| *انتهت صلاحية الرمز؛ أنشئ رمزًا جديدًا في Client Portal* | أنشئ رمز Flex Web Service جديدًا والصقه. |
| *الرمز غير صالح* | انسخ الرمز مرة أخرى؛ فالرمز الجديد يحل محل القديم. |
| *الرمز مقيّد بعنوان IP آخر* | أنشئ الرمز دون تقييد بعنوان IP. |
| *معرّف Flex Query غير صالح* | تحقق من الرقم؛ يجب أن يكون الاستعلام Activity Flex Query تابعًا لتسجيل الدخول هذا. |
| *أضف إلى Flex Query القسم Net Asset Value (NAV) Summary in Base مع الحقلين Report Date وTotal* | عدّل الاستعلام وأضف القسم والحقلين. |
| *أضف إلى Flex Query الحقل Currency من القسم Account Information* | عدّل الاستعلام وأضف الحقل. |
| *أعاد الاستعلام عدد حسابات هو N؛ أنشئ Flex Query واحدًا لكل حساب* | أنشئ استعلامًا يغطي حسابًا واحدًا. |
| *التقرير ليس جاهزًا بعد؛ حدّث مرة أخرى بعد دقيقة* | ما زال Interactive Brokers يُنشئ التقرير؛ حدّث مرة أخرى. |
| *تم رفض الوصول؛ تحقق من مفتاح المزود أو الحصة* | رمز OANDA خاطئ أو مُلغى، أو زوج مفاتيح Trading 212 خاطئ أو تنقصه صلاحية Account data. |
| *ليست لدى SnapTrade قيمة إجمالية لهذا الحساب بعد؛ زامن الاتصال ثم أعد المحاولة* | لم يزامن SnapTrade الوسيط بعد؛ حدّث مرة أخرى لاحقًا. |
| *لا توجد حسابات مربوطة بعد. اربط وسيطًا عبر SnapTrade أولًا.* | افتح Connection Portal من المحرر واربط وسيطًا. |
| *قيمة الحساب السالبة … غير مدعومة* | الحساب مدين؛ ولا يضيف شيئًا إلى مدخراتك. |
| *رفض tastytrade رمز التحديث أو سر العميل؛ أنشئ تفويضًا جديدًا* | أنشئ تفويضًا جديدًا للتطبيق والصق رمز التحديث الخاص به؛ وتحقق من سر العميل. |
| *لم يفتح Capital.com جلسة؛ تحقق من مفتاح API وتسجيل الدخول وكلمة مرور المفتاح* | المفتاح أو البريد الإلكتروني أو كلمة المرور المخصصة للمفتاح خاطئة، أو انتهت صلاحية المفتاح. |
| *الحساب غير موجود؛ اختره مرة أخرى* | لم يعد الوسيط يسرد هذا الحساب؛ عدّله واختره من **جلب الحسابات**. |

تبقى القيمة السابقة ظاهرة بعد أي من هذه الحالات، مع وسمها بأنها قديمة.
