---
layout: default
lang: ur
base: "/ur"
key: "accounts"
title: بروکر اور فاریکس اکاؤنٹس
class: doc
---
# بروکر اور فاریکس اکاؤنٹس

Capital بروکریج یا فاریکس اکاؤنٹ کی کل مالیت اسی طرح پڑھ سکتی ہے جیسے کرپٹو والیٹ کی۔ آپ اکاؤنٹ کو **بروکر اکاؤنٹ** قسم کے کھاتے کے طور پر کسی گلک میں شامل کرتے ہیں، اور ہر ریفریش پر ایپ اکاؤنٹ کی نیٹ ایسٹ ویلیو اس کی بنیادی کرنسی میں حاصل کرتی ہے۔ ایپ صرف پڑھتی ہے: یہ بروکر کا رپورٹنگ انٹرفیس اس ٹوکن کے ساتھ استعمال کرتی ہے جو آپ خود بناتے ہیں، کبھی کوئی آرڈر نہیں دیتی، نہیں بدلتی اور نہ منسوخ کرتی ہے، اور پیسہ کبھی منتقل نہیں کرتی۔

Capital صرف ایسے انٹرفیس سے جڑتی ہے جن کی اسناد طویل مدت کی ہوں: ایسا ٹوکن یا کلید جو آپ ایک بار بناتے ہیں اور جو منسوخ کرنے تک، آپ کی منتخب کردہ میعاد تک، یا کم از کم کئی مہینے تک کارآمد رہتا ہے (T-Invest کے ٹوکن تین مہینے استعمال نہ ہونے پر ختم ہو جاتے ہیں، ALOR کے ٹوکن ایک سال بعد)۔ نیچے دیا گیا ہر بروکر اس سے قطع نظر دستیاب ہے کہ ایپ کی زبان کون سی ہے۔ اس وقت معاون:

| بروکر | استعمال ہونے والا انٹرفیس | کیا پڑھا جاتا ہے |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (صرف رپورٹ حاصل کرنے کے لیے) | آخری کاروباری دن کی نیٹ ایسٹ ویلیو، اکاؤنٹ کی بنیادی کرنسی میں |
| [OANDA](#oanda) | v20 REST API، fxTrade کے لائیو اکاؤنٹس | ریفریش کے وقت کی نیٹ ایسٹ ویلیو، اکاؤنٹ کی کرنسی میں |
| [Trading 212](#trading-212) | Public API، Invest اور Stocks ISA اکاؤنٹس | ریفریش کے وقت اکاؤنٹ کی کل مالیت، اکاؤنٹ کی بنیادی کرنسی میں |
| [SnapTrade](#snaptrade) | SnapTrade Personal، ایک ایگریگیٹر جو کئی بروکرز کا احاطہ کرتا ہے | اکاؤنٹ کی کل مالیت، جیسی بروکر SnapTrade کو بتاتا ہے، اکاؤنٹ کی کرنسی میں |
| [Alpaca](#alpaca) | Trading API، لائیو اکاؤنٹس | ایکویٹی (کیش اور پوزیشنز کا مجموعہ)، امریکی ڈالر میں |
| [Tradier](#tradier) | Brokerage API | کل ایکویٹی، امریکی ڈالر میں |
| [tastytrade](#tastytrade) | Open API، ذاتی OAuth گرانٹ کے ساتھ | نیٹ لیکویڈیٹنگ ویلیو، امریکی ڈالر میں |
| [Public.com](#public) | Individual API | اکاؤنٹ کی کل مالیت، امریکی ڈالر میں |
| [eToro](#etoro) | Public API | منتخب اکاؤنٹ کا بیلنس (ٹریڈنگ اکاؤنٹ کے لیے: کیش اور لگائی گئی پوزیشنز کا مجموعہ)، اس کی کرنسی میں |
| [Indexa Capital](#indexa-capital) | REST API، صرف پڑھنے والا ٹوکن | آخری ویلیویشن کی تاریخ پر پورٹ فولیو کا کل، اکاؤنٹ کی کرنسی میں |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | پورٹ فولیو کی کل مالیت، روبل میں |
| [ALOR](#alor) | ALOR OpenAPI | ماسکو ایکسچینج پر پورٹ فولیو کی ویلیویشن، روبل میں |
| [Capital.com](#capital-com) | Public API، لائیو اکاؤنٹس | کھلے منافع اور نقصان سمیت بیلنس، اکاؤنٹ کی کرنسی میں |
| [Akahu](#akahu) | Akahu پرسنل ایپ، نیوزی لینڈ کا ایگریگیٹر | منسلک اکاؤنٹ کا بیلنس (Sharesies، Hatch، Kernel، KiwiSaver وغیرہ)، اس کی کرنسی میں |

## شروع کرنے سے پہلے {#before-you-start}

- **ڈیوائس سے کیا باہر جاتا ہے۔** ہر ریفریش پر ایپ آپ کا ایکسیس ٹوکن اور اکاؤنٹ یا کوئری id اس بروکر کو HTTPS پر بھیجتی ہے۔ ہر درخواست کی طرح بروکر آپ کا IP ایڈریس دیکھتا ہے۔
- **اسناد کہاں رکھی جاتی ہیں۔** بروکرز ٹیب ← اسناد میں۔ یہ Android Keystore میں رکھی گئی کلید سے انکرپٹ ہوتا ہے، آپ کے ڈیٹا فولڈر میں کبھی نہیں لکھا جاتا، اور ایکسپورٹس اور سسٹم بیک اپ میں شامل نہیں ہوتا۔ ہر بروکر کی اسناد کا ایک سیٹ اس بروکر کے لیے آپ کے شامل کردہ تمام اکاؤنٹس کے لیے کافی ہے۔
- **آپ کے فولڈر میں کیا محفوظ ہوتا ہے۔** اکاؤنٹ id، آخری پڑھی گئی مالیت اور وہ وقت جب اسے پڑھا گیا۔ بروکر کی طرف سے اس کے علاوہ کچھ نہیں۔
- **بروکر کی اسکرینیں بدل سکتی ہیں۔** نیچے کے مراحل اکتوبر 2026 تک بروکرز کی ویب سائٹس سے مطابقت رکھتے ہیں۔ بروکرز وقتاً فوقتاً مینو کے نام بدلتے اور سیٹنگز منتقل کرتے ہیں، اس لیے عمل کرتے وقت کوئی مرحلہ کچھ مختلف دکھائی دے سکتا ہے۔ ہر سیکشن میں دیا گیا بروکر کی اپنی دستاویزات کا لنک مستند ذریعہ ہے: اگر یہاں کا کوئی مرحلہ اب مطابقت نہ رکھے تو بروکر کے صفحے پر وہی اصطلاح تلاش کریں۔

## Interactive Brokers {#interactive-brokers}

Capital **Flex Web Service** استعمال کرتی ہے، جو Interactive Brokers کا پہلے سے ترتیب دی گئی رپورٹس حاصل کرنے کا انٹرفیس ہے۔ اس کا ٹوکن صرف رپورٹس بنا اور ڈاؤن لوڈ کر سکتا ہے؛ اس سے لاگ اِن، ٹریڈنگ یا رقم نکالنا ممکن نہیں۔ Capital کسی Activity Flex Query کی **Net Asset Value (NAV) Summary in Base** مانگتی ہے اور تازہ ترین رپورٹ کی تاریخ کا Total لیتی ہے، اس لیے مالیت آخری کاروباری دن کے اختتام کی ہوتی ہے۔

### 1. Flex Query بنائیں

1. [Client Portal](https://www.interactivebrokers.com/portal) میں لاگ اِن کریں اور **Performance & Reports → Flex Queries** کھولیں (کچھ اکاؤنٹس پر مینو کا نام *Reporting* ہے)۔
2. **Activity Flex Query** کے تحت **+** (Create) دبائیں۔ کوئری کو کوئی نام دیں، مثلاً `Capital`۔
3. **Sections** کی فہرست میں بالکل یہی دو سیکشن اور فیلڈز فعال کریں (سیکشن کی تمام فیلڈز منتخب کرنا بھی چلے گا):
   - **Account Information**: *Account ID*، *Currency*۔
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*، *Total*۔
4. **Delivery Configuration** میں **Format** کو `XML` اور **Period** کو `Last Business Day` پر رکھیں۔ باقی آپشنز ڈیفالٹ پر چھوڑ سکتے ہیں۔
5. کوئری محفوظ کریں، پھر اس کے برابر **i** (معلومات) آئیکن دبائیں اور **Query ID** نوٹ کر لیں، جو ایک نمبر ہے۔

کوئری ایک ہی اکاؤنٹ کا احاطہ کرے۔ اگر آپ کے جڑے ہوئے اکاؤنٹس یا ایڈوائزر والا ڈھانچہ ہے تو ہر اکاؤنٹ کے لیے الگ کوئری بنائیں اور بناتے وقت صرف وہی اکاؤنٹ منتخب کریں۔

### 2. Flex Web Service فعال کریں اور ٹوکن بنائیں

1. اسی **Flex Queries** صفحے پر **Flex Web Service Configuration** کھولیں۔
2. **Flex Web Service Status** آن کریں اور محفوظ کریں۔ ایک ٹوکن بن جاتا ہے۔
3. ٹوکن کتنی دیر کارآمد رہے، یہ چننے کے لیے **Generate New Token** دبائیں: 6 گھنٹے سے 1 سال تک۔ **Valid for IP address** کو خالی چھوڑیں، کیونکہ فون کا ایڈریس بدلتا رہتا ہے۔ نیا ٹوکن بنانے سے پچھلا ٹوکن ناکارہ ہو جاتا ہے۔
4. ٹوکن کاپی کریں۔

### 3. Capital میں جوڑیں

1. **بروکرز ← اسناد ← ایکسیس ٹوکن: Interactive Brokers**، ٹوکن پیسٹ کریں اور محفوظ کریں۔
2. **بروکرز ← +**: نام درج کریں، **بروکر** کو Interactive Brokers پر رکھیں، **Flex Query id** درج کریں اور محفوظ کریں۔
3. گلک کھولیں، **کھاتہ شامل کریں**، **ٹریکنگ** کو **بروکر اکاؤنٹ** پر رکھیں، اکاؤنٹ منتخب کریں اور محفوظ کریں۔
4. **ریفریش کریں** دبائیں۔ پہلی بار میں آدھے منٹ تک لگ سکتا ہے، کیونکہ رپورٹ درخواست پر تیار کی جاتی ہے۔

ٹوکن کی میعاد ختم ہونے پر ریفریش یہ بتاتا ہے: *ٹوکن کی میعاد ختم ہو گئی ہے؛ Client Portal میں نیا ٹوکن بنائیں*۔ نیا ٹوکن بنائیں اور اسناد میں پیسٹ کریں۔ Interactive Brokers ایک ٹوکن سے فی سیکنڈ ایک اور فی منٹ دس رپورٹ درخواستوں کی اجازت دیتا ہے، جس سے ریفریش کبھی تجاوز نہیں کرتا۔

Interactive Brokers کی دستاویزات: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [ایکسیس ٹوکن فعال کرنا اور بنانا](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Flex Query بنانا](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query کا حوالہ](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital OANDA v20 REST API کا **اکاؤنٹ سمری** کال کرتی ہے اور اکاؤنٹ کی NAV (بیلنس جمع غیر حقیقی منافع یا نقصان) اکاؤنٹ کی کرنسی میں محفوظ کرتی ہے۔ صرف لائیو **fxTrade** اکاؤنٹس معاون ہیں؛ پریکٹس اکاؤنٹس بچت نہیں ہیں۔

**OANDA کا ذاتی ایکسیس ٹوکن صرف پڑھنے والا نہیں ہوتا۔** یہ آپ کے لاگ اِن کے ہر ذیلی اکاؤنٹ تک مکمل API رسائی دیتا ہے، ٹریڈنگ سمیت۔ Capital صرف اکاؤنٹ سمری کال کرتی ہے، لیکن جس کے ہاتھ یہ ٹوکن لگ جائے وہ اس سے ٹریڈ کر سکتا ہے۔ اسے پاس ورڈ کی طرح سمجھیں: اسے صرف Capital میں پیسٹ کریں، اور فون کھو جانے پر OANDA پورٹل میں اسے منسوخ کر دیں۔

### 1. ٹوکن بنائیں

1. اپنے OANDA fxTrade اکاؤنٹ مینجمنٹ پورٹل میں لاگ اِن کریں۔
2. **My Services → Manage API Access** کھولیں (پرانے پورٹل پر: *My Account → My Services → Manage API Access*)۔
3. API لائسنس قبول کریں اور **Generate** دبائیں۔ ٹوکن کاپی کریں؛ OANDA اسے دوبارہ نہیں دکھاتا۔ اگر آپ اسے کھو دیں تو وہیں اسے منسوخ کریں اور نیا بنائیں۔

### 2. اکاؤنٹ id تلاش کریں

v20 اکاؤنٹ id کی شکل `001-001-1234567-001` ہوتی ہے، ہائفنز کے ساتھ۔ یہ اسی پورٹل میں ہر ذیلی اکاؤنٹ کے برابر درج ہوتی ہے، اور fxTrade پلیٹ فارم پر اکاؤنٹ کی تفصیلات میں بھی۔

### 3. Capital میں جوڑیں

1. **بروکرز ← اسناد ← ایکسیس ٹوکن: OANDA**، ٹوکن پیسٹ کریں اور محفوظ کریں۔
2. **بروکرز ← +**: نام درج کریں، **بروکر** کو OANDA پر رکھیں، **OANDA اکاؤنٹ id** درج کریں اور محفوظ کریں۔
3. گلک کھولیں، **کھاتہ شامل کریں**، **ٹریکنگ** کو **بروکر اکاؤنٹ** پر رکھیں، اکاؤنٹ منتخب کریں اور محفوظ کریں۔
4. **ریفریش کریں** دبائیں۔

جس مارجن اکاؤنٹ کی NAV منفی ہو اسے بچت میں شمار کرنے کے بجائے خرابی کے طور پر ظاہر کیا جاتا ہے۔

OANDA کی دستاویزات: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [تصدیق اور ذاتی ایکسیس ٹوکن](https://developer.oanda.com/rest-live-v20/authentication/) · [اکاؤنٹ اینڈ پوائنٹس](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital Trading 212 Public API کا **اکاؤنٹ سمری** کال کرتی ہے اور اکاؤنٹ کی کل مالیت اکاؤنٹ کی بنیادی کرنسی میں محفوظ کرتی ہے۔ API **Invest** اور **Stocks ISA** اکاؤنٹس کا احاطہ کرتی ہے؛ کلیدوں کا جوڑا ایک اکاؤنٹ کا ہوتا ہے، اور Capital کلیدوں کا ایک ہی جوڑا رکھتی ہے، اس لیے وہ ایک Trading 212 اکاؤنٹ پڑھتی ہے۔

### 1. API کلید بنائیں

1. Trading 212 کی ایپ یا ویب سائٹ میں مینو (**☰**) ← **Settings** ← **API (Beta)** کھولیں اور خطرے کی وارننگ قبول کریں۔
2. **Generate API key** دبائیں۔ اسے کوئی نام دیں، صرف **Account data** کی اجازت (پڑھنا) رکھیں، اور IP رسائی *Unrestricted* چنیں (فون کا ایڈریس بدلتا رہتا ہے)۔
3. جمع کرائیں۔ دونوں قدریں کاپی کریں: **API Key** اور **API Secret Key**۔ سیکرٹ صرف ایک بار دکھایا جاتا ہے؛ اگر آپ اسے کھو دیں تو کلید حذف کریں اور نیا جوڑا بنائیں۔

### 2. Capital میں جوڑیں

1. **بروکرز ← اسناد ← API کلید: Trading 212** اور **API سیکرٹ: Trading 212**، ہر قدر پیسٹ کریں۔
2. **بروکرز ← +**: نام درج کریں، **بروکر** کو Trading 212 پر رکھیں، **Trading 212 اکاؤنٹ نمبر** درج کریں (ایپ میں دکھایا گیا اکاؤنٹ id، صرف ہندسے) اور محفوظ کریں۔
3. گلک کھولیں، **کھاتہ شامل کریں**، **ٹریکنگ** کو **بروکر اکاؤنٹ** پر رکھیں، اکاؤنٹ منتخب کریں اور محفوظ کریں۔
4. **ریفریش کریں** دبائیں۔ Trading 212 ہر 5 سیکنڈ میں ایک سمری درخواست کی اجازت دیتا ہے۔

Trading 212 کی دستاویزات: [Public API](https://docs.trading212.com/api) · [اپنی API کلید کیسے حاصل کریں](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) ایک ایگریگیٹر ہے: آپ بروکریج اکاؤنٹ کو SnapTrade سے ایک بار جوڑتے ہیں، اور SnapTrade اسے آپ کے لیے پڑھتا ہے۔ یہ ان کئی بروکرز کا احاطہ کرتا ہے جن کی اپنی کوئی عوامی API نہیں۔ Capital **SnapTrade Personal** استعمال کرتی ہے، جو آپ کے اپنے اکاؤنٹس کے لیے مفت پلان ہے، آپ کے اپنے کلائنٹ id اور کنزیومر کلید کے ساتھ۔ اس پلان پر ڈیٹا SnapTrade تقریباً دن میں ایک بار تازہ کرتا ہے۔

کیا بھیجا جاتا ہے: آپ کا کلائنٹ id، اور دستخط کے طور پر کنزیومر کلید خود کچھ نہیں (درخواستوں پر اسی سے دستخط کیے جاتے ہیں)۔ آپ کے بروکر سے کنکشن Capital کے پاس نہیں بلکہ SnapTrade کے پاس ہوتا ہے؛ اس کنکشن پر اس کی شرائط اور رازداری کی پالیسی لاگو ہوتی ہیں۔

### 1. API کلید بنائیں

1. [SnapTrade dashboard](https://dashboard.snaptrade.com/signup) پر سائن اپ کریں اور **Personal** پلان چنیں۔
2. ڈیش بورڈ میں ایک API کلید بنائیں۔ **کلائنٹ id** اور **کنزیومر کلید** کاپی کریں؛ کنزیومر کلید صرف ایک بار دکھائی جاتی ہے۔

### 2. Capital میں جوڑیں

1. **بروکرز ← اسناد ← کلائنٹ id: SnapTrade** اور **کنزیومر کلید: SnapTrade**، ہر قدر پیسٹ کریں۔
2. **بروکرز ← +**: نام درج کریں اور **بروکر** کو SnapTrade پر رکھیں۔
3. **SnapTrade کے ذریعے بروکریج جوڑیں** دبائیں۔ SnapTrade Connection Portal براؤزر میں کھلتا ہے؛ وہاں اپنے بروکر میں لاگ اِن کریں (لنک 5 منٹ تک کارآمد رہتا ہے)۔ Capital میں واپس آئیں۔
4. **اکاؤنٹس حاصل کریں** دبائیں اور اکاؤنٹ منتخب کریں؛ اس کا id **SnapTrade اکاؤنٹ id** فیلڈ میں بھر جاتا ہے۔ محفوظ کریں۔
5. گلک کھولیں، **کھاتہ شامل کریں**، **ٹریکنگ** کو **بروکر اکاؤنٹ** پر رکھیں، اکاؤنٹ منتخب کریں اور محفوظ کریں، پھر **ریفریش کریں**۔

جس اکاؤنٹ کی سنکرونائزیشن SnapTrade نے ابھی مکمل نہیں کی وہ *SnapTrade کے پاس اس اکاؤنٹ کی کل مالیت ابھی موجود نہیں* بتاتا ہے؛ کچھ دیر بعد دوبارہ ریفریش کریں۔

SnapTrade کی دستاویزات: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [معاون بروکریجز](https://snaptrade.com/brokerage-integrations) · [قیمتیں](https://snaptrade.com/pricing)

## Capital میں اکاؤنٹ جوڑنا {#connect}

نیچے کے سیکشن بتاتے ہیں کہ ہر بروکر کے پاس اسناد کیسے بنائی جاتی ہیں۔ Capital میں مراحل سب کے لیے ایک جیسے ہیں:

1. **بروکرز ← اسناد**: بروکر کے اسناد والے بٹن دبائیں اور ہر قدر پیسٹ کریں۔
2. **بروکرز ← +**: نام درج کریں، **بروکر** منتخب کریں، پھر **اکاؤنٹس حاصل کریں** دبائیں اور اکاؤنٹ منتخب کریں (یا اس کا id ٹائپ کریں) اور محفوظ کریں۔
3. گلک کھولیں، **کھاتہ شامل کریں**، **ٹریکنگ** کو **بروکر اکاؤنٹ** پر رکھیں، اکاؤنٹ منتخب کریں اور محفوظ کریں۔ **ریفریش کریں** دبائیں۔

## Alpaca {#alpaca}

Alpaca ہر اکاؤنٹ کے لیے ایک کلید id اور ایک سیکرٹ جاری کرتا ہے؛ یہ آپ کے دوبارہ بنانے تک کارآمد رہتے ہیں۔ صرف لائیو اکاؤنٹس پڑھے جاتے ہیں: پیپر اکاؤنٹ کی کلیدیں لائیو API پر کام نہیں کرتیں۔

1. [Alpaca dashboard](https://app.alpaca.markets) میں لاگ اِن کریں، اپنے لائیو اکاؤنٹ پر جائیں اور ہوم پیج پر **API Keys** کے تحت **Generate New Keys** دبائیں۔
2. **API Key ID** اور **Secret Key** کاپی کریں؛ سیکرٹ صرف ایک بار دکھایا جاتا ہے۔
3. Capital میں انہیں **API کلید: Alpaca** اور **API سیکرٹ: Alpaca** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔ **اکاؤنٹس حاصل کریں** کلید کا اکاؤنٹ نمبر دکھاتا ہے۔

Alpaca کی دستاویزات: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

آپ کی Tradier سیٹنگز کا API ٹوکن کبھی ختم نہیں ہوتا۔

1. Tradier میں لاگ اِن کریں اور [Settings → API Access](https://web.tradier.com/user/api) کھولیں۔ اپنے بروکریج اکاؤنٹ کا **API Access Token** کاپی کریں (سینڈ باکس کا ٹوکن نہیں)۔
2. Capital میں اسے **ایکسیس ٹوکن: Tradier** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔

Tradier کی دستاویزات: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade ذاتی OAuth گرانٹ استعمال کرتا ہے: آپ اپنے لیے ایک ایپلیکیشن اور ایک گرانٹ بناتے ہیں جس کا ریفریش ٹوکن کبھی ختم نہیں ہوتا۔ Capital ہر ریفریش پر اسے 15 منٹ کے ایکسیس ٹوکن سے بدلتی ہے۔

1. [my.tastytrade.com](https://my.tastytrade.com) پر **Manage → My Profile → API → OAuth Applications** کھولیں اور **+ New OAuth client** دبائیں۔ اسے کوئی نام دیں، کوئی بھی HTTPS ری ڈائریکٹ URI (مثلاً `https://capital.fimych.dev`) اور صرف **read** اسکوپ رکھیں۔ محفوظ کریں اور **Client Secret** کاپی کریں؛ یہ صرف ایک بار دکھایا جاتا ہے۔
2. ایپلیکیشن کے برابر **Manage** دبائیں، پھر **Create Grant**، اور **refresh token** کاپی کریں۔
3. Capital میں انہیں **ریفریش ٹوکن: tastytrade** اور **کلائنٹ سیکرٹ: tastytrade** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔

tastytrade کی دستاویزات: [OAuth2 اور ذاتی گرانٹس](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Public کی Individual API آپ کے اپنے اکاؤنٹس کے لیے ہے۔ سیکرٹ کلید طویل مدت کی اور منسوخ کی جا سکنے والی ہے؛ Capital ہر ریفریش پر اسے پانچ منٹ کے ایکسیس ٹوکن سے بدلتی ہے۔

1. Public کی ویب ایپ میں اپنی سیٹنگز کا **API** صفحہ کھولیں اور **سیکرٹ کلید** بنائیں۔
2. Capital میں اسے **سیکرٹ کلید: Public.com** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔

Public کی دستاویزات: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

eToro کی کلیدیں طویل مدت کی ہوتی ہیں؛ آپ انہیں ختم ہونے کی تاریخ اور IP کی فہرست دے سکتے ہیں، اور انہیں صرف پڑھنے والی بنا سکتے ہیں۔ آپ کا eToro اکاؤنٹ تصدیق شدہ ہونا چاہیے۔

1. eToro میں **Settings → Trading → API Key Management** کھولیں اور **Create New Key** دبائیں۔ **Real** ماحول، **Read** اجازت، کوئی IP فہرست نہیں، اور چاہیں تو ختم ہونے کی تاریخ چنیں۔ SMS کوڈ سے تصدیق کریں۔
2. **Public API Key** اور **User Key** کاپی کریں؛ یوزر کلید صرف ایک بار دکھائی جاتی ہے۔
3. Capital میں انہیں **پبلک API کلید: eToro** اور **یوزر کلید: eToro** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔ **اکاؤنٹس حاصل کریں** آپ کے ٹریڈنگ، کیش اور دیگر eToro اکاؤنٹس کی فہرست دیتا ہے۔

eToro کی دستاویزات: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Indexa کے نجی علاقے کا ٹوکن صرف پڑھنے والا ہے۔ یہ آپ کی ای میل، پاس ورڈ اور ڈیوائس سے بندھا ہوتا ہے: پاس ورڈ بدلنے کے بعد اسے دوبارہ بنائیں۔

1. Indexa کے نجی علاقے میں **یوزر سیٹنگز ← ایپلیکیشنز** کھولیں اور ٹوکن کاپی کریں۔
2. Capital میں اسے **ایکسیس ٹوکن: Indexa Capital** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔ پنشن اور سرمایہ کاری دونوں طرح کے اکاؤنٹس فہرست میں آتے ہیں۔

Indexa فنڈز کی ویلیویشن ہر کاروباری دن میں ایک بار کرتا ہے؛ مشاہدے کی تاریخ وہی ویلیویشن کی تاریخ ہے۔

Indexa Capital کی دستاویزات: [REST API](https://indexacapital.com/en/api-rest-v1) · [API سے جڑنا](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Bank کی T-Invest API وہ ٹوکن قبول کرتی ہے جو آپ سرمایہ کاری کی سیٹنگز میں جاری کرتے ہیں۔ ٹوکن آخری استعمال کے تین مہینے بعد ختم ہو جاتا ہے اور اسے جاری ہونے کے سات دن کے اندر استعمال کرنا ضروری ہے؛ ہفتہ وار ریفریش اسے زندہ رکھتا ہے۔ **صرف پڑھنے والا** ٹوکن چنیں۔

1. [T-Invest کی سیٹنگز](https://www.tbank.ru/invest/settings/) کھولیں اور ایکسچینج کے لیے **صرف پڑھنے** کی رسائی کے ساتھ **T-Invest API ٹوکن** جاری کریں (تمام اکاؤنٹس یا ایک)۔ ٹوکن جاری کرنے کے لیے کوڈ سے ٹریڈز کی تصدیق بند ہونی چاہیے۔ ٹوکن کاپی کریں؛ یہ صرف ایک بار دکھایا جاتا ہے۔
2. Capital میں اسے **ایکسیس ٹوکن: T-Invest** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔

T-Bank یہ API روس کے Russian Trusted Root CA کے تحت چلاتا ہے، جو Android میں شامل نہیں۔ Capital اس سرٹیفکیٹ پر صرف T-Invest API کے پتے (`invest-public-api.tbank.ru`) کے لیے بھروسا کرتی ہے، کسی اور کنکشن کے لیے نہیں۔

T-Invest کی دستاویزات: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR ایک سال کے لیے کارآمد ریفریش ٹوکن جاری کرتا ہے؛ Capital ہر ریفریش پر اسے 30 منٹ کے ایکسیس ٹوکن سے بدلتی ہے۔ ALOR صرف پڑھنے والا ٹوکن نہیں دیتا: ٹوکن ٹریڈ کر سکتا ہے، Capital صرف پڑھتی ہے۔

1. [ALOR ڈویلپر پورٹل](https://alor.dev) میں سائن اِن کریں، اپنا ٹریڈنگ اکاؤنٹ منسلک کریں، **API Access Tokens** کھولیں اور **Create Token** دبائیں۔ ریفریش ٹوکن کاپی کریں۔
2. Capital میں اسے **ریفریش ٹوکن: ALOR** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔ **اکاؤنٹس حاصل کریں** اکاؤنٹ کے پورٹ فولیوز کی فہرست دیتا ہے (اسٹاک مارکیٹ D…، کرنسی مارکیٹ G…، ڈیریویٹوز 7500…)؛ ہر پورٹ فولیو کے لیے ایک شامل کریں۔

ALOR کی دستاویزات: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Capital.com کی کلیدیں بطور ڈیفالٹ ایک سال کے لیے، یا آپ کی چنی ہوئی تاریخ تک کارآمد ہوتی ہیں۔ ان میں ٹریڈنگ کے حقوق ہوتے ہیں (Capital.com صرف پڑھنے والی کلیدیں نہیں دیتا)؛ Capital صرف پڑھتی ہے۔ کلید کا اپنا پاس ورڈ ہوتا ہے، جو آپ کے اکاؤنٹ کا پاس ورڈ نہیں ہے۔

1. ٹو فیکٹر تصدیق آن کریں، پھر **Settings → API integrations** کھولیں اور **Generate API key** دبائیں۔ اسے لیبل اور **custom password** دیں، میعاد برقرار رکھیں یا طے کریں، اور 2FA کوڈ سے تصدیق کریں۔ کلید کاپی کریں؛ یہ صرف ایک بار دکھائی جاتی ہے۔
2. Capital میں **API کلید: Capital.com** پیسٹ کریں، اپنی لاگ اِن ای میل **لاگ اِن ای میل: Capital.com** کے طور پر اور custom password **API کلید کا پاس ورڈ: Capital.com** کے طور پر، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔ صرف لائیو اکاؤنٹس پڑھے جاتے ہیں۔

Capital.com کی دستاویزات: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) نیوزی لینڈ کے بینکوں، سرمایہ کاری پلیٹ فارمز اور KiwiSaver اسکیموں کو جوڑتا ہے؛ ایک مفت پرسنل ایپ آپ کے اپنے اکاؤنٹس پڑھتی ہے۔ Akahu ڈیٹا تقریباً دن میں ایک بار تازہ کرتا ہے۔

1. [my.akahu.nz](https://my.akahu.nz) پر سائن اپ کریں اور اپنے پرووائیڈرز جوڑیں (مثلاً Sharesies، Hatch، Kernel، Simplicity، Milford یا آپ کی KiwiSaver اسکیم)۔
2. **Developers** صفحہ کھولیں، ڈویلپر کی شرائط قبول کریں اور **App ID Token** اور **User Access Token** کاپی کریں۔
3. Capital میں انہیں **App ID ٹوکن: Akahu** اور **یوزر ایکسیس ٹوکن: Akahu** کے طور پر پیسٹ کریں، پھر [Capital میں اکاؤنٹ جوڑنا](#connect) پر عمل کریں۔

Akahu کی دستاویزات: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [معاون پرووائیڈرز](https://developers.akahu.nz/docs/integrations)

## مارکیٹ کے لحاظ سے مقبول بروکرز {#by-market}

Capital کی زبانوں کی مارکیٹوں کے سب سے زیادہ استعمال ہونے والے بروکرز کو کیسے جوڑا جا سکتا ہے، اکتوبر 2026 تک۔ *براہِ راست* کا مطلب اوپر کا کوئی سیکشن ہے؛ *SnapTrade* کا مطلب [SnapTrade](#snaptrade) کے ذریعے؛ ورنہ وجہ بتائی گئی ہے کہ انہیں کیوں نہیں پڑھا جا سکتا، اور بیلنس **دستی** کھاتے کے طور پر رکھا جا سکتا ہے۔

| مارکیٹ | بروکر | طریقہ |
|---|---|---|
| امریکہ | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | براہِ راست |
| امریکہ | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| امریکہ | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | کوئی عوامی API نہیں |
| کینیڈا | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| کینیڈا | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | کوئی عوامی API نہیں |
| برطانیہ اور آئرلینڈ | Trading 212, eToro, Interactive Brokers | براہِ راست |
| برطانیہ اور آئرلینڈ | AJ Bell | SnapTrade |
| برطانیہ اور آئرلینڈ | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | کوئی عوامی API نہیں |
| برطانیہ اور آئرلینڈ | IG | ممکن نہیں: ہر سیشن کے لیے اکاؤنٹ کا پاس ورڈ درکار ہے |
| یورپ | Indexa Capital (اسپین), eToro, Trading 212, Interactive Brokers | براہِ راست |
| یورپ | DEGIRO, BUX | SnapTrade |
| یورپ | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | سرمایہ کاری کے لیے کوئی عوامی API نہیں |
| یورپ | XTB | ممکن نہیں: API مارچ 2025 میں بند کر دی گئی |
| یورپ | Saxo, comdirect | ممکن نہیں: صرف مختصر مدت کے ٹوکن یا TAN سیشن |
| یورپ | Bitpanda, Freedom24 | ممکن نہیں: API اکاؤنٹ کی کل مالیت نہیں دیتی |
| روس اور قازقستان | T-Invest, ALOR | براہِ راست |
| روس اور قازقستان | BCS | ممکن نہیں: کل مالیت نہیں، اور اس کا ٹوکن 90 دن بعد ختم ہو جاتا ہے |
| روس اور قازقستان | Finam | ابھی نہیں: اکاؤنٹ کی مالیت کی کرنسی دستاویز میں درج نہیں |
| روس اور قازقستان | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | کوئی عوامی API نہیں، یا اس میں کل مالیت نہیں |
| بھارت | Zerodha, Upstox | SnapTrade (SEBI کے قواعد ہر روز API سیشن ختم کر دیتے ہیں، اس لیے کنکشن کی بار بار تجدید ضروری ہے) |
| بھارت | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | ممکن نہیں: SEBI کے قواعد ہر API سیشن روزانہ ختم کر دیتے ہیں |
| پاکستان اور بنگلہ دیش | تمام ایکسچینج بروکرز | کوئی عوامی API نہیں |
| چین، ہانگ کانگ اور تائیوان | moomoo | SnapTrade |
| چین، ہانگ کانگ اور تائیوان | Futu, Tiger Brokers, Longbridge | ابھی نہیں: کلید کی میعاد یا جواب کی شکل پوری طرح دستاویز میں درج نہیں، یا کلیدوں کو صرف پڑھنے تک محدود نہیں کیا جا سکتا |
| چین، ہانگ کانگ اور تائیوان | East Money, Huatai, CITIC, Yuanta, Fubon | کوئی عوامی ویب API نہیں (صرف ڈیسک ٹاپ ٹرمینل یا سرٹیفکیٹ SDK) |
| جاپان | OANDA Japan (وہ اکاؤنٹس جو API رسائی کے اہل ہیں) | براہِ راست، OANDA کے طور پر |
| جاپان | SBI Securities, Rakuten Securities, Monex, Matsui | کوئی عوامی API نہیں |
| آسٹریلیا اور نیوزی لینڈ | CommSec, Stake | SnapTrade |
| آسٹریلیا اور نیوزی لینڈ | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver اسکیمیں | Akahu (نیوزی لینڈ کے اکاؤنٹس) |
| مشرقِ وسطیٰ اور افریقہ | eToro | براہِ راست |
| مشرقِ وسطیٰ اور افریقہ | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | افراد کے لیے کوئی عوامی API نہیں |
| جنوب مشرقی ایشیا | Stockbit, Ajaib, Bibit, IPOT, VPS | کوئی عوامی API نہیں |
| جنوب مشرقی ایشیا | SSI, TCBS, DNSE | ممکن نہیں: 8 گھنٹے کے ٹوکن جن کے ساتھ ایک بار کا کوڈ درکار ہے، یا صرف کیش بیلنس |
| لاطینی امریکہ | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | افراد کے لیے کوئی عوامی API نہیں، یا صرف پاس ورڈ سے لاگ اِن |
| فاریکس اور CFD | OANDA, Capital.com | براہِ راست |
| فاریکس اور CFD | MetaTrader بروکرز (XM, Exness, Pepperstone, IC Markets, Admirals) | ممکن نہیں: HTTPS پر پڑھنے کی رسائی نہیں |
| فاریکس اور CFD | cTrader بروکرز, FXCM, Forex.com | ممکن نہیں: ایپ رجسٹریشن، متروک API یا پاس ورڈ سے لاگ اِن |

## دیگر بروکرز {#other-brokers}

Capital صرف ایسے انٹرفیس سے جڑتی ہے جو فون سے HTTPS پر کام کریں، جن کا ٹوکن آپ خود بنا سکیں، اور جو صرف پڑھنے دیں، ٹریڈ کرنے نہیں۔ اس لیے فی الحال یہ خارج ہیں:

- **MetaTrader 4 اور 5** اکاؤنٹس۔ انویسٹر پاس ورڈ صرف پڑھنے کی رسائی دیتا ہے، لیکن صرف MetaTrader ٹرمینل کے اندر؛ بروکرز اس کے لیے کوئی HTTPS انٹرفیس شائع نہیں کرتے۔
- وہ بروکرز جن کی API کے لیے کمپیوٹر پر چلتا ہوا پروگرام (مثلاً Interactive Brokers کا Client Portal Web API گیٹ وے؛ Capital اس کے بجائے Flex Web Service استعمال کرتی ہے) یا OAuth ایپلی کیشن کی رجسٹریشن درکار ہو۔
- وہ بروکرز جن کی API OAuth کے ذریعے صرف مختصر مدت کے ٹوکن جاری کرتی ہے، مثلاً Saxo Bank (ایکسیس ٹوکن 20 منٹ چلتے ہیں؛ ڈویلپر پورٹل کا 24-hour token صرف سمولیشن ماحول کے لیے ہے)۔
- وہ بینک اور بروکرز جن کی کوئی عوامی API نہیں۔

ان میں سے کئی بروکرز [SnapTrade](#snaptrade) کے ذریعے کور ہوتے ہیں۔ بصورتِ دیگر بیلنس **دستی** کھاتے کے طور پر درج کریں اور اسٹیٹمنٹ دیکھ کر نمبر اپ ڈیٹ کرتے رہیں۔ اگر آپ کا بروکر ٹوکن پر مبنی کوئی سادہ HTTPS اینڈ پوائنٹ دیتا ہے جو اکاؤنٹ کی مالیت پڑھتا ہے تو اس کی دستاویزات کے لنک کے ساتھ [مسئلہ درج کریں]({{ site.repo }}/issues)۔ Capital میں ہر بروکر ایک چھوٹا پلگ اِن ہے؛ ڈیولپرز [پلگ اِن گائیڈ]({{ site.repo }}/blob/main/BROKER-PLUGINS.md) پر عمل کر کے نیا بروکر شامل کر سکتے ہیں۔

## پیغامات اور کیا کرنا ہے {#messages}

| پیغام | کیا کریں |
|---|---|
| *Interactive Brokers کے لیے بروکرز اسکرین پر اسناد درج کریں* / *OANDA کے لیے … اسناد درج کریں* | ٹوکن، کلید یا کلائنٹ id بروکرز ٹیب پر اسناد کے تحت پیسٹ کریں۔ |
| *ٹوکن کی میعاد ختم ہو گئی ہے؛ Client Portal میں نیا ٹوکن بنائیں* | Flex Web Service کا نیا ٹوکن بنائیں اور پیسٹ کریں۔ |
| *ٹوکن غلط ہے* | ٹوکن دوبارہ کاپی کریں؛ نیا ٹوکن پرانے کی جگہ لے لیتا ہے۔ |
| *ٹوکن کسی دوسرے IP ایڈریس تک محدود ہے* | ٹوکن بغیر IP پابندی کے بنائیں۔ |
| *Flex Query id غلط ہے* | نمبر چیک کریں؛ کوئری اسی لاگ اِن کی Activity Flex Query ہونی چاہیے۔ |
| *Flex Query میں Net Asset Value (NAV) Summary in Base سیکشن کو Report Date اور Total کے ساتھ شامل کریں* | کوئری میں ترمیم کریں اور سیکشن اور فیلڈز شامل کریں۔ |
| *Flex Query میں Account Information کی Currency فیلڈ شامل کریں* | کوئری میں ترمیم کریں اور فیلڈ شامل کریں۔ |
| *کوئری سے N اکاؤنٹس واپس آئے؛ ہر اکاؤنٹ کے لیے الگ Flex Query بنائیں* | ایسی کوئری بنائیں جو ایک اکاؤنٹ کا احاطہ کرے۔ |
| *اسٹیٹمنٹ ابھی تیار نہیں؛ ایک منٹ بعد دوبارہ ریفریش کریں* | Interactive Brokers ابھی رپورٹ تیار کر رہا ہے؛ دوبارہ ریفریش کریں۔ |
| *رسائی سے انکار؛ پرووائیڈر کی کلید یا کوٹا چیک کریں* | OANDA کا ٹوکن غلط ہے یا منسوخ ہو چکا ہے، یا Trading 212 کی کلیدوں کا جوڑا غلط ہے یا اس میں Account data کی اجازت نہیں۔ |
| *SnapTrade کے پاس اس اکاؤنٹ کی کل مالیت ابھی موجود نہیں؛ کنکشن سنک کریں اور دوبارہ کوشش کریں* | SnapTrade نے بروکریج ابھی سنک نہیں کی؛ کچھ دیر بعد دوبارہ ریفریش کریں۔ |
| *ابھی کوئی اکاؤنٹ منسلک نہیں۔ پہلے SnapTrade کے ذریعے بروکریج جوڑیں۔* | ایڈیٹر سے Connection Portal کھولیں اور بروکر جوڑیں۔ |
| *اکاؤنٹ کی منفی مالیت … معاون نہیں* | اکاؤنٹ ڈیبٹ میں ہے؛ اس سے آپ کی بچت میں کچھ اضافہ نہیں ہوتا۔ |
| *tastytrade نے ریفریش ٹوکن یا کلائنٹ سیکرٹ مسترد کر دیا؛ نئی گرانٹ بنائیں* | ایپلیکیشن کے لیے نئی گرانٹ بنائیں اور اس کا ریفریش ٹوکن پیسٹ کریں؛ کلائنٹ سیکرٹ چیک کریں۔ |
| *Capital.com نے سیشن نہیں کھولا؛ API کلید، لاگ اِن اور کلید کا پاس ورڈ چیک کریں* | کلید، ای میل یا کلید کا custom password غلط ہے، یا کلید کی میعاد ختم ہو چکی ہے۔ |
| *اکاؤنٹ نہیں ملا؛ اسے دوبارہ منتخب کریں* | بروکر اب اس اکاؤنٹ کو فہرست میں نہیں دیتا؛ اس میں ترمیم کریں اور **اکاؤنٹس حاصل کریں** سے منتخب کریں۔ |

ان میں سے کسی کے بعد بھی پچھلی مالیت نظر آتی رہتی ہے، پرانی ہونے کے نشان کے ساتھ۔
