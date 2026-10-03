---
layout: default
lang: hi
base: "/hi"
key: "accounts"
title: ब्रोकर और फ़ॉरेक्स खाते
class: doc
---
# ब्रोकर और फ़ॉरेक्स खाते

Capital ब्रोकरेज या फ़ॉरेक्स खाते का कुल मूल्य उसी तरह पढ़ सकता है जैसे वह क्रिप्टो वॉलेट पढ़ता है। आप खाते को गुल्लक में **ब्रोकर खाता** प्रकार की होल्डिंग के रूप में जोड़ते हैं, और हर रीफ़्रेश पर ऐप खाते की नेट एसेट वैल्यू (net asset value) खाते की बेस करेंसी में लाता है। ऐप सिर्फ़ पढ़ता है: यह ब्रोकर के रिपोर्टिंग इंटरफ़ेस का इस्तेमाल उस टोकन से करता है जिसे आप खुद बनाते हैं; यह कभी कोई ऑर्डर नहीं देता, बदलता या रद्द करता, और कभी पैसा नहीं भेजता।

Capital सिर्फ़ ऐसे इंटरफ़ेस से जुड़ता है जिनके क्रेडेंशियल लंबे समय तक मान्य रहते हैं: ऐसा टोकन या कुंजी जिसे आप एक बार बनाते हैं और जो आपके रद्द करने तक, आपकी चुनी समाप्ति तक, या कम से कम महीनों तक मान्य रहती है (T-Invest के टोकन बिना इस्तेमाल के तीन महीने बाद, ALOR के टोकन एक साल बाद समाप्त हो जाते हैं)। नीचे दिया हर ब्रोकर ऐप की भाषा कोई भी हो, उपलब्ध है। अभी समर्थित:

| ब्रोकर | इस्तेमाल होने वाला इंटरफ़ेस | क्या पढ़ा जाता है |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (केवल रिपोर्ट प्राप्त करने के लिए) | आख़िरी कारोबारी दिन की नेट एसेट वैल्यू, खाते की बेस करेंसी में |
| [OANDA](#oanda) | v20 REST API, fxTrade लाइव खाते | रीफ़्रेश के समय की नेट एसेट वैल्यू, खाते की करेंसी में |
| [Trading 212](#trading-212) | Public API, Invest और Stocks ISA खाते | रीफ़्रेश के समय खाते का कुल मूल्य, खाते की मुख्य करेंसी में |
| [SnapTrade](#snaptrade) | SnapTrade Personal, कई ब्रोकरों को कवर करने वाला एग्रीगेटर | खाते का कुल मूल्य, जैसा ब्रोकर उसे SnapTrade को बताता है, खाते की करेंसी में |
| [Alpaca](#alpaca) | Trading API, लाइव खाते | इक्विटी (नकद और पोज़िशन मिलाकर), अमेरिकी डॉलर में |
| [Tradier](#tradier) | Brokerage API | कुल इक्विटी, अमेरिकी डॉलर में |
| [tastytrade](#tastytrade) | निजी OAuth ग्रांट वाला Open API | नेट लिक्विडेटिंग वैल्यू, अमेरिकी डॉलर में |
| [Public.com](#public) | Individual API | खाते का कुल मूल्य, अमेरिकी डॉलर में |
| [eToro](#etoro) | Public API | चुने खाते का बैलेंस (ट्रेडिंग खाते के लिए: नकद और निवेशित पोज़िशन), उसकी करेंसी में |
| [Indexa Capital](#indexa-capital) | REST API, केवल-पढ़ने वाला टोकन | आख़िरी मूल्यांकन तिथि पर पोर्टफ़ोलियो का कुल, खाते की करेंसी में |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | पोर्टफ़ोलियो का कुल मूल्य, रूबल में |
| [ALOR](#alor) | ALOR OpenAPI | मॉस्को एक्सचेंज पर पोर्टफ़ोलियो का मूल्यांकन, रूबल में |
| [Capital.com](#capital-com) | Public API, लाइव खाते | खुले लाभ-हानि सहित बैलेंस, खाते की करेंसी में |
| [Akahu](#akahu) | Akahu निजी ऐप, न्यूज़ीलैंड का एग्रीगेटर | जुड़े खाते का बैलेंस (Sharesies, Hatch, Kernel, KiwiSaver और अन्य), उसकी करेंसी में |

## शुरू करने से पहले {#before-you-start}

- **डिवाइस से क्या बाहर जाता है।** हर रीफ़्रेश पर ऐप आपका एक्सेस टोकन और खाता या क्वेरी id उस ब्रोकर को HTTPS पर भेजता है। किसी भी अनुरोध की तरह ब्रोकर आपका IP पता देखता है।
- **क्रेडेंशियल कहाँ रहते हैं।** ब्रोकर टैब → क्रेडेंशियल। वे Android Keystore में रखी कुंजी से एन्क्रिप्ट होते हैं, आपके डेटा फ़ोल्डर में कभी नहीं लिखे जाते, और एक्सपोर्ट तथा सिस्टम बैकअप में शामिल नहीं होते। एक ब्रोकर के क्रेडेंशियल का एक सेट उस ब्रोकर के लिए जोड़े गए हर खाते के लिए काम करता है।
- **आपके फ़ोल्डर में क्या सहेजा जाता है।** खाता id, पढ़ा गया आख़िरी मान और वह कब पढ़ा गया। ब्रोकर से और कुछ नहीं।
- **ब्रोकर की स्क्रीन बदल सकती हैं।** नीचे के चरण अक्टूबर 2026 तक ब्रोकरों की वेबसाइटों से मेल खाते हैं। ब्रोकर समय-समय पर मेनू के नाम बदलते और सेटिंग इधर-उधर करते हैं, इसलिए चरण अपनाते समय कोई चरण थोड़ा अलग दिख सकता है। हर खंड में दिए गए लिंक वाले ब्रोकर के अपने दस्तावेज़ ही प्रामाणिक स्रोत हैं: अगर यहाँ का कोई चरण अब मेल न खाए, तो ब्रोकर के पेज पर वही शब्द खोजें।

## Interactive Brokers {#interactive-brokers}

Capital **Flex Web Service** इस्तेमाल करता है, जो पहले से कॉन्फ़िगर की गई रिपोर्ट प्राप्त करने के लिए Interactive Brokers का इंटरफ़ेस है। इसका टोकन सिर्फ़ रिपोर्ट बना और डाउनलोड कर सकता है; वह लॉगिन, ट्रेडिंग या निकासी नहीं कर सकता। Capital Activity Flex Query का **Net Asset Value (NAV) Summary in Base** माँगता है और सबसे ताज़ा रिपोर्ट तिथि का Total लेता है, इसलिए मान आख़िरी कारोबारी दिन के क्लोज़ का होता है।

### 1. Flex Query बनाएँ

1. [Client Portal](https://www.interactivebrokers.com/portal) में लॉग इन करें और **Performance & Reports → Flex Queries** खोलें (कुछ खातों में मेनू का नाम *Reporting* है)।
2. **Activity Flex Query** के नीचे **+** (Create) दबाएँ। क्वेरी को एक नाम दें, जैसे `Capital`।
3. **Sections** सूची में ठीक ये दो सेक्शन और फ़ील्ड चालू करें (सेक्शन के सभी फ़ील्ड चुनना भी चलता है):
   - **Account Information**: *Account ID*, *Currency*।
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*।
4. **Delivery Configuration** (डिलीवरी कॉन्फ़िगरेशन) में **Format** को `XML` और **Period** को `Last Business Day` पर सेट करें। बाकी विकल्प डिफ़ॉल्ट रह सकते हैं।
5. क्वेरी सहेजें, फिर उसके बगल का **i** (जानकारी) आइकन दबाएँ और **Query ID** नोट करें, जो एक संख्या है।

क्वेरी एक ही खाते को कवर करनी चाहिए। अगर आपके लिंक किए हुए खाते या एडवाइज़र ढाँचा है, तो हर खाते के लिए एक अलग क्वेरी बनाएँ और बनाते समय सिर्फ़ वही खाता चुनें।

### 2. Flex Web Service चालू करें और टोकन बनाएँ

1. उसी **Flex Queries** पेज पर **Flex Web Service Configuration** खोलें।
2. **Flex Web Service Status** चालू करें और सहेजें। एक टोकन बन जाता है।
3. टोकन कितने समय तक मान्य रहे, यह चुनने के लिए **Generate New Token** दबाएँ: 6 घंटे से 1 साल तक कुछ भी। फ़ोन के लिए **Valid for IP address** खाली छोड़ें, क्योंकि उसका पता बदलता रहता है। नया टोकन बनाने से पिछला टोकन अमान्य हो जाता है।
4. टोकन कॉपी करें।

### 3. Capital में जोड़ें

1. **ब्रोकर → क्रेडेंशियल → एक्सेस टोकन: Interactive Brokers**, टोकन पेस्ट करें और सहेजें।
2. **ब्रोकर → +**: एक नाम डालें, **ब्रोकर** में Interactive Brokers चुनें, **Flex Query id** डालें और सहेजें।
3. गुल्लक खोलें, **होल्डिंग जोड़ें**, **ट्रैकिंग** को **ब्रोकर खाता** पर सेट करें, खाता चुनें और सहेजें।
4. **रीफ़्रेश करें** दबाएँ। पहली बार में आधे मिनट तक लगता है, क्योंकि रिपोर्ट माँगने पर बनती है।

टोकन की अवधि समाप्त होने पर रीफ़्रेश *टोकन की अवधि समाप्त हो गई है; Client Portal में नया टोकन बनाएँ* बताता है: नया टोकन बनाएँ और उसे क्रेडेंशियल में पेस्ट करें। Interactive Brokers एक टोकन से प्रति सेकंड एक और प्रति मिनट दस रिपोर्ट अनुरोध की अनुमति देता है, जिसे एक रीफ़्रेश कभी पार नहीं करता।

Interactive Brokers के दस्तावेज़: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [एक्सेस टोकन चालू करना और बनाना](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Flex Query बनाना](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query संदर्भ](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital OANDA v20 REST API का **account summary** कॉल करता है और खाते की NAV (बैलेंस और अवास्तविक लाभ या हानि का जोड़) खाते की करेंसी में सहेजता है। सिर्फ़ लाइव **fxTrade** खाते समर्थित हैं; प्रैक्टिस खाते बचत नहीं हैं।

**OANDA का पर्सनल एक्सेस टोकन केवल-पढ़ने वाला नहीं है।** वह आपके लॉगिन के हर सब-अकाउंट का पूरा API ऐक्सेस देता है, ट्रेडिंग समेत। Capital सिर्फ़ account summary कॉल करता है, पर जिसके हाथ यह टोकन लग जाए वह इससे ट्रेड कर सकता है। इसे पासवर्ड की तरह मानें: इसे सिर्फ़ Capital में पेस्ट करें, और फ़ोन खो जाए तो OANDA पोर्टल में इसे रद्द कर दें।

### 1. टोकन बनाएँ

1. अपने OANDA fxTrade खाता प्रबंधन पोर्टल में लॉग इन करें।
2. **My Services → Manage API Access** खोलें (पुराने पोर्टल पर: *My Account → My Services → Manage API Access*)।
3. API लाइसेंस स्वीकार करें और **Generate** दबाएँ। टोकन कॉपी करें; OANDA उसे दोबारा नहीं दिखाता। खो जाए तो उसे वहीं रद्द करें और नया बनाएँ।

### 2. खाता id ढूँढें

v20 खाता id का रूप `001-001-1234567-001` है, हाइफ़न के साथ। वह उसी पोर्टल में हर सब-अकाउंट के बगल में सूचीबद्ध है, और fxTrade प्लैटफ़ॉर्म पर खाते के विवरण में भी।

### 3. Capital में जोड़ें

1. **ब्रोकर → क्रेडेंशियल → एक्सेस टोकन: OANDA**, टोकन पेस्ट करें और सहेजें।
2. **ब्रोकर → +**: एक नाम डालें, **ब्रोकर** में OANDA चुनें, **OANDA खाता id** डालें और सहेजें।
3. गुल्लक खोलें, **होल्डिंग जोड़ें**, **ट्रैकिंग** को **ब्रोकर खाता** पर सेट करें, खाता चुनें और सहेजें।
4. **रीफ़्रेश करें** दबाएँ।

जिस मार्जिन खाते की NAV ऋणात्मक है, उसे बचत गिनने के बजाय त्रुटि के रूप में दिखाया जाता है।

OANDA के दस्तावेज़: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [ऑथेंटिकेशन और पर्सनल एक्सेस टोकन](https://developer.oanda.com/rest-live-v20/authentication/) · [Account एंडपॉइंट](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital Trading 212 Public API का **account summary** कॉल करता है और खाते का कुल मूल्य खाते की मुख्य करेंसी में सहेजता है। API **Invest** और **Stocks ISA** खातों को कवर करता है; एक कुंजी-जोड़ी एक खाते की होती है और Capital एक ही कुंजी-जोड़ी रखता है, इसलिए वह एक Trading 212 खाता पढ़ता है।

### 1. API कुंजी बनाएँ

1. Trading 212 ऐप या वेबसाइट में मेनू (**☰**) → **Settings** → **API (Beta)** खोलें और जोखिम की चेतावनी स्वीकार करें।
2. **Generate API key** दबाएँ। उसे एक नाम दें, केवल **Account data** अनुमति (पढ़ना) रखें, और IP ऐक्सेस *Unrestricted* चुनें (फ़ोन का पता बदलता रहता है)।
3. सबमिट करें। दोनों मान कॉपी करें: **API Key** और **API Secret Key**। सीक्रेट सिर्फ़ एक बार दिखता है; खो जाए तो कुंजी हटाएँ और नई जोड़ी बनाएँ।

### 2. Capital में जोड़ें

1. **ब्रोकर → क्रेडेंशियल → API कुंजी: Trading 212** और **API सीक्रेट: Trading 212**, हर मान पेस्ट करें।
2. **ब्रोकर → +**: एक नाम डालें, **ब्रोकर** में Trading 212 चुनें, **Trading 212 खाता नंबर** डालें (ऐप में दिखने वाला खाता id, केवल अंक) और सहेजें।
3. गुल्लक खोलें, **होल्डिंग जोड़ें**, **ट्रैकिंग** को **ब्रोकर खाता** पर सेट करें, खाता चुनें और सहेजें।
4. **रीफ़्रेश करें** दबाएँ। Trading 212 हर 5 सेकंड में एक summary अनुरोध की अनुमति देता है।

Trading 212 के दस्तावेज़: [Public API](https://docs.trading212.com/api) · [API कुंजी कैसे पाएँ](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) एक एग्रीगेटर (कई ब्रोकरों से आपकी ओर से जुड़ने वाली सेवा) है: आप ब्रोकरेज खाते को SnapTrade से एक बार जोड़ते हैं, और SnapTrade उसे आपके लिए पढ़ता है। यह ऐसे कई ब्रोकरों को कवर करता है जिनका अपना कोई सार्वजनिक API नहीं है। Capital **SnapTrade Personal** इस्तेमाल करता है, जो आपके अपने खातों के लिए मुफ़्त प्लान है, और आपके अपने क्लाइंट id और कंज़्यूमर कुंजी के साथ। इस प्लान में डेटा SnapTrade लगभग दिन में एक बार रीफ़्रेश करता है।

क्या भेजा जाता है: आपका क्लाइंट id और, हस्ताक्षर के रूप में, कंज़्यूमर कुंजी का खुद कुछ भी नहीं (अनुरोधों पर उसी से हस्ताक्षर होते हैं)। आपके ब्रोकर से कनेक्शन SnapTrade के पास है, Capital के पास नहीं; उस कनेक्शन पर उसकी शर्तें और गोपनीयता नीति लागू होती हैं।

### 1. API कुंजी बनाएँ

1. [SnapTrade डैशबोर्ड](https://dashboard.snaptrade.com/signup) पर साइन अप करें और **Personal** प्लान चुनें।
2. डैशबोर्ड में API कुंजी बनाएँ। **क्लाइंट id** और **कंज़्यूमर कुंजी** कॉपी करें; कंज़्यूमर कुंजी सिर्फ़ एक बार दिखती है।

### 2. Capital में जोड़ें

1. **ब्रोकर → क्रेडेंशियल → क्लाइंट id: SnapTrade** और **कंज़्यूमर कुंजी: SnapTrade**, हर मान पेस्ट करें।
2. **ब्रोकर → +**: एक नाम डालें और **ब्रोकर** को SnapTrade पर सेट करें।
3. **SnapTrade के ज़रिए ब्रोकरेज जोड़ें** दबाएँ। SnapTrade का Connection Portal ब्राउज़र में खुलता है; वहाँ अपने ब्रोकर में लॉग इन करें (लिंक 5 मिनट तक मान्य है)। फिर Capital में लौटें।
4. **खाते प्राप्त करें** दबाएँ और खाता चुनें; उसका id **SnapTrade खाता id** फ़ील्ड में भर जाता है। सहेजें।
5. गुल्लक खोलें, **होल्डिंग जोड़ें**, **ट्रैकिंग** को **ब्रोकर खाता** पर सेट करें, खाता चुनें और सहेजें, फिर **रीफ़्रेश करें**।

जिस खाते को SnapTrade ने अभी सिंक पूरा नहीं किया है, वह *SnapTrade के पास इस खाते का कुल मूल्य अभी नहीं है* बताता है; बाद में फिर रीफ़्रेश करें।

SnapTrade के दस्तावेज़: [शुरुआत](https://docs.snaptrade.com/docs/getting-started) · [Personal बनाम Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [समर्थित ब्रोकरेज](https://snaptrade.com/brokerage-integrations) · [कीमतें](https://snaptrade.com/pricing)

## Capital में खाता जोड़ना {#connect}

नीचे के खंड बताते हैं कि हर ब्रोकर पर क्रेडेंशियल कैसे बनाएँ। Capital में सभी के लिए चरण एक जैसे हैं:

1. **ब्रोकर → क्रेडेंशियल**: ब्रोकर के क्रेडेंशियल बटन दबाएँ और हर मान पेस्ट करें।
2. **ब्रोकर → +**: एक नाम डालें, **ब्रोकर** चुनें, फिर **खाते प्राप्त करें** दबाएँ और खाता चुनें (या उसका id डालें) और सहेजें।
3. गुल्लक खोलें, **होल्डिंग जोड़ें**, **ट्रैकिंग** को **ब्रोकर खाता** पर सेट करें, खाता चुनें और सहेजें। **रीफ़्रेश करें** दबाएँ।

## Alpaca {#alpaca}

Alpaca हर खाते के लिए एक कुंजी id और एक सीक्रेट जारी करता है; वे आपके दोबारा बनाने तक मान्य रहते हैं। सिर्फ़ लाइव खाते पढ़े जाते हैं: पेपर खाते की कुंजियाँ लाइव API पर काम नहीं करतीं।

1. [Alpaca डैशबोर्ड](https://app.alpaca.markets) में लॉग इन करें, अपने लाइव खाते पर जाएँ और होम पेज पर **API Keys** के अंतर्गत **Generate New Keys** दबाएँ।
2. **API Key ID** और **Secret Key** कॉपी करें; सीक्रेट सिर्फ़ एक बार दिखता है।
3. Capital में उन्हें **API कुंजी: Alpaca** और **API सीक्रेट: Alpaca** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें। **खाते प्राप्त करें** कुंजी का खाता नंबर दिखाता है।

Alpaca के दस्तावेज़: [ऑथेंटिकेशन](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

आपकी Tradier सेटिंग का API टोकन कभी समाप्त नहीं होता।

1. Tradier में लॉग इन करें और [Settings → API Access](https://web.tradier.com/user/api) खोलें। अपने ब्रोकरेज खाते का **API Access Token** कॉपी करें (सैंडबॉक्स टोकन नहीं)।
2. Capital में उसे **एक्सेस टोकन: Tradier** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें।

Tradier के दस्तावेज़: [ऑथेंटिकेशन](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade निजी OAuth ग्रांट इस्तेमाल करता है: आप अपने लिए एक एप्लिकेशन और एक ग्रांट बनाते हैं, जिसका रीफ़्रेश टोकन कभी समाप्त नहीं होता। हर रीफ़्रेश पर Capital उसे 15 मिनट के एक्सेस टोकन से बदलता है।

1. [my.tastytrade.com](https://my.tastytrade.com) पर **Manage → My Profile → API → OAuth Applications** खोलें और **+ New OAuth client** दबाएँ। उसे एक नाम दें, कोई भी HTTPS रीडायरेक्ट URI (उदाहरण के लिए `https://capital.fimych.dev`) और सिर्फ़ **read** स्कोप रखें। सहेजें और **Client Secret** कॉपी करें; वह सिर्फ़ एक बार दिखता है।
2. एप्लिकेशन के आगे **Manage** दबाएँ, फिर **Create Grant**, और **refresh token** कॉपी करें।
3. Capital में उन्हें **रीफ़्रेश टोकन: tastytrade** और **क्लाइंट सीक्रेट: tastytrade** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें।

tastytrade के दस्तावेज़: [OAuth2 और निजी ग्रांट](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Public का Individual API आपके अपने खातों के लिए है। सीक्रेट कुंजी लंबे समय तक मान्य रहती है और रद्द की जा सकती है; हर रीफ़्रेश पर Capital उसे पाँच मिनट के एक्सेस टोकन से बदलता है।

1. Public के वेब ऐप में अपनी सेटिंग का **API** पेज खोलें और एक **secret key** बनाएँ।
2. Capital में उसे **सीक्रेट कुंजी: Public.com** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें।

Public के दस्तावेज़: [क्विकस्टार्ट](https://public.com/api/docs/quickstart) · [एक्सेस टोकन](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [पोर्टफ़ोलियो](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

eToro की कुंजियाँ लंबे समय तक मान्य रहती हैं; आप उन पर समाप्ति तिथि और IP सूची लगा सकते हैं, और उन्हें केवल-पढ़ने वाली बना सकते हैं। आपका eToro खाता सत्यापित होना चाहिए।

1. eToro में **Settings → Trading → API Key Management** खोलें और **Create New Key** दबाएँ। **Real** परिवेश, **Read** अनुमति, कोई IP सूची नहीं, और चाहें तो समाप्ति तिथि चुनें। SMS कोड से पुष्टि करें।
2. **Public API Key** और **User Key** कॉपी करें; यूज़र कुंजी सिर्फ़ एक बार दिखती है।
3. Capital में उन्हें **सार्वजनिक API कुंजी: eToro** और **यूज़र कुंजी: eToro** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें। **खाते प्राप्त करें** आपके ट्रेडिंग, कैश और अन्य eToro खाते सूचीबद्ध करता है।

eToro के दस्तावेज़: [ऑथेंटिकेशन](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [शुरुआत](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Indexa के निजी क्षेत्र का टोकन केवल-पढ़ने वाला है। वह आपके ई-मेल, पासवर्ड और डिवाइस से बँधा है: पासवर्ड बदलने के बाद उसे दोबारा बनाएँ।

1. Indexa के निजी क्षेत्र में **उपयोगकर्ता सेटिंग → ऐप्लिकेशन** खोलें और टोकन कॉपी करें।
2. Capital में उसे **एक्सेस टोकन: Indexa Capital** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें। पेंशन और निवेश, दोनों खाते सूचीबद्ध होते हैं।

Indexa फ़ंड का मूल्यांकन हर कारोबारी दिन में एक बार करता है; प्रेक्षण की तारीख़ वही मूल्यांकन तिथि है।

Indexa Capital के दस्तावेज़: [REST API](https://indexacapital.com/en/api-rest-v1) · [API से कनेक्ट करना](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Bank का T-Invest API वह टोकन स्वीकार करता है जो आप निवेश सेटिंग में जारी करते हैं। टोकन आख़िरी इस्तेमाल के तीन महीने बाद समाप्त हो जाता है और जारी होने के सात दिनों के भीतर इस्तेमाल होना चाहिए; हर हफ़्ते रीफ़्रेश उसे चालू रखता है। **केवल-पढ़ने वाला** टोकन चुनें।

1. [T-Invest सेटिंग](https://www.tbank.ru/invest/settings/) खोलें और एक्सचेंज के लिए **केवल-पढ़ने** की पहुँच वाला **T-Invest API टोकन** जारी करें (सभी खाते या एक)। उसे जारी करने के लिए कोड से सौदों की पुष्टि बंद होनी चाहिए। टोकन कॉपी करें; वह सिर्फ़ एक बार दिखता है।
2. Capital में उसे **एक्सेस टोकन: T-Invest** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें।

T-Bank यह API रूस के Trusted Root CA के अंतर्गत चलाता है, जो Android में शामिल नहीं है। Capital उस प्रमाणपत्र पर सिर्फ़ T-Invest API के पते (`invest-public-api.tbank.ru`) के लिए भरोसा करता है, किसी और कनेक्शन के लिए नहीं।

T-Invest के दस्तावेज़: [टोकन](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR एक साल मान्य रीफ़्रेश टोकन जारी करता है; हर रीफ़्रेश पर Capital उसे 30 मिनट के एक्सेस टोकन से बदलता है। ALOR केवल-पढ़ने वाला टोकन नहीं देता: टोकन से ट्रेड हो सकता है, Capital सिर्फ़ पढ़ता है।

1. [ALOR डेवलपर पोर्टल](https://alor.dev) में साइन इन करें, अपना ट्रेडिंग खाता जोड़ें, **API Access Tokens** खोलें और **Create Token** दबाएँ। रीफ़्रेश टोकन कॉपी करें।
2. Capital में उसे **रीफ़्रेश टोकन: ALOR** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें। **खाते प्राप्त करें** खाते के पोर्टफ़ोलियो सूचीबद्ध करता है (शेयर बाज़ार D…, मुद्रा बाज़ार G…, डेरिवेटिव 7500…); हर पोर्टफ़ोलियो के लिए एक जोड़ें।

ALOR के दस्तावेज़: [रीफ़्रेश टोकन](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [एक्सेस टोकन](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Capital.com की कुंजियाँ डिफ़ॉल्ट रूप से एक साल, या आपकी चुनी तारीख़ तक मान्य रहती हैं। उनमें ट्रेडिंग के अधिकार होते हैं (Capital.com में केवल-पढ़ने वाली कुंजियाँ नहीं हैं); Capital सिर्फ़ पढ़ता है। कुंजी का अपना पासवर्ड होता है, जो आपके खाते का पासवर्ड नहीं है।

1. टू-फ़ैक्टर ऑथेंटिकेशन चालू करें, फिर **Settings → API integrations** खोलें और **Generate API key** दबाएँ। उसे एक लेबल और एक **custom password** दें, समाप्ति रखें या तय करें, और 2FA कोड से पुष्टि करें। कुंजी कॉपी करें; वह सिर्फ़ एक बार दिखती है।
2. Capital में **API कुंजी: Capital.com** पेस्ट करें, अपना लॉगिन ई-मेल **लॉगिन ई-मेल: Capital.com** में और कस्टम पासवर्ड **API कुंजी पासवर्ड: Capital.com** में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें। सिर्फ़ लाइव खाते पढ़े जाते हैं।

Capital.com के दस्तावेज़: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) न्यूज़ीलैंड के बैंकों, निवेश प्लेटफ़ॉर्म और KiwiSaver योजनाओं को जोड़ता है; एक मुफ़्त निजी ऐप आपके अपने खाते पढ़ता है। Akahu डेटा लगभग दिन में एक बार रीफ़्रेश करता है।

1. [my.akahu.nz](https://my.akahu.nz) पर साइन अप करें और अपने प्रदाता जोड़ें (उदाहरण के लिए Sharesies, Hatch, Kernel, Simplicity, Milford या आपकी KiwiSaver योजना)।
2. **Developers** पेज खोलें, डेवलपर शर्तें स्वीकार करें और **App ID Token** तथा **User Access Token** कॉपी करें।
3. Capital में उन्हें **ऐप ID टोकन: Akahu** और **यूज़र एक्सेस टोकन: Akahu** के रूप में पेस्ट करें, फिर [Capital में खाता जोड़ना](#connect) का पालन करें।

Akahu के दस्तावेज़: [निजी ऐप](https://developers.akahu.nz/docs/personal-apps) · [खाते](https://developers.akahu.nz/reference/get_accounts) · [समर्थित प्रदाता](https://developers.akahu.nz/docs/integrations)

## बाज़ार के अनुसार लोकप्रिय ब्रोकर {#by-market}

Capital की भाषाओं वाले बाज़ारों के सबसे ज़्यादा इस्तेमाल होने वाले ब्रोकर कैसे जोड़े जा सकते हैं, अक्टूबर 2026 तक। *सीधे* का मतलब ऊपर का कोई खंड; *SnapTrade* का मतलब [SnapTrade](#snaptrade) के ज़रिए; अन्यथा वह कारण जिससे उसे पढ़ा नहीं जा सकता, और बैलेंस **मैन्युअल** होल्डिंग के रूप में रखा जा सकता है।

| बाज़ार | ब्रोकर | कैसे |
|---|---|---|
| अमेरिका | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | सीधे |
| अमेरिका | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| अमेरिका | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | कोई सार्वजनिक API नहीं |
| कनाडा | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| कनाडा | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | कोई सार्वजनिक API नहीं |
| यूनाइटेड किंगडम और आयरलैंड | Trading 212, eToro, Interactive Brokers | सीधे |
| यूनाइटेड किंगडम और आयरलैंड | AJ Bell | SnapTrade |
| यूनाइटेड किंगडम और आयरलैंड | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | कोई सार्वजनिक API नहीं |
| यूनाइटेड किंगडम और आयरलैंड | IG | संभव नहीं: हर सत्र के लिए खाते का पासवर्ड चाहिए |
| यूरोप | Indexa Capital (स्पेन), eToro, Trading 212, Interactive Brokers | सीधे |
| यूरोप | DEGIRO, BUX | SnapTrade |
| यूरोप | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | निवेश के लिए कोई सार्वजनिक API नहीं |
| यूरोप | XTB | संभव नहीं: API मार्च 2025 में बंद कर दिया गया |
| यूरोप | Saxo, comdirect | संभव नहीं: सिर्फ़ अल्पकालिक टोकन या TAN सत्र |
| यूरोप | Bitpanda, Freedom24 | संभव नहीं: API खाते का कुल मूल्य नहीं लौटाता |
| रूस और कज़ाखस्तान | T-Invest, ALOR | सीधे |
| रूस और कज़ाखस्तान | BCS | संभव नहीं: कुल मूल्य नहीं है, और उसका टोकन 90 दिन में समाप्त हो जाता है |
| रूस और कज़ाखस्तान | Finam | अभी नहीं: खाते के मूल्य की करेंसी दस्तावेज़ में नहीं है |
| रूस और कज़ाखस्तान | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | कोई सार्वजनिक API नहीं, या उसमें कुल मूल्य नहीं |
| भारत | Zerodha, Upstox | SnapTrade (SEBI के नियम रोज़ API सत्र समाप्त कर देते हैं, इसलिए कनेक्शन को बार-बार नवीनीकृत करना पड़ता है) |
| भारत | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | संभव नहीं: SEBI के नियम हर API सत्र रोज़ समाप्त कर देते हैं |
| पाकिस्तान और बांग्लादेश | सभी एक्सचेंज ब्रोकर | कोई सार्वजनिक API नहीं |
| चीन, हांगकांग और ताइवान | moomoo | SnapTrade |
| चीन, हांगकांग और ताइवान | Futu, Tiger Brokers, Longbridge | अभी नहीं: कुंजी की अवधि या उत्तर का प्रारूप पूरी तरह दर्ज नहीं, या कुंजियाँ केवल पढ़ने तक सीमित नहीं की जा सकतीं |
| चीन, हांगकांग और ताइवान | East Money, Huatai, CITIC, Yuanta, Fubon | कोई सार्वजनिक वेब API नहीं (सिर्फ़ डेस्कटॉप टर्मिनल या प्रमाणपत्र वाले SDK) |
| जापान | OANDA Japan (API पहुँच के योग्य खाते) | सीधे, OANDA की तरह |
| जापान | SBI Securities, Rakuten Securities, Monex, Matsui | कोई सार्वजनिक API नहीं |
| ऑस्ट्रेलिया और न्यूज़ीलैंड | CommSec, Stake | SnapTrade |
| ऑस्ट्रेलिया और न्यूज़ीलैंड | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver योजनाएँ | Akahu (न्यूज़ीलैंड के खाते) |
| मध्य पूर्व और अफ़्रीका | eToro | सीधे |
| मध्य पूर्व और अफ़्रीका | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | आम लोगों के लिए कोई सार्वजनिक API नहीं |
| दक्षिण-पूर्व एशिया | Stockbit, Ajaib, Bibit, IPOT, VPS | कोई सार्वजनिक API नहीं |
| दक्षिण-पूर्व एशिया | SSI, TCBS, DNSE | संभव नहीं: एक-बार वाले कोड के साथ 8 घंटे के टोकन, या सिर्फ़ नकद बैलेंस |
| लैटिन अमेरिका | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | आम लोगों के लिए कोई सार्वजनिक API नहीं, या सिर्फ़ पासवर्ड वाले लॉगिन |
| फ़ॉरेक्स और CFD | OANDA, Capital.com | सीधे |
| फ़ॉरेक्स और CFD | MetaTrader ब्रोकर (XM, Exness, Pepperstone, IC Markets, Admirals) | संभव नहीं: HTTPS पर पढ़ने की पहुँच नहीं |
| फ़ॉरेक्स और CFD | cTrader ब्रोकर, FXCM, Forex.com | संभव नहीं: ऐप पंजीकरण, अप्रचलित API या पासवर्ड लॉगिन |

## अन्य ब्रोकर {#other-brokers}

Capital सिर्फ़ ऐसे इंटरफ़ेस से जुड़ता है जो फ़ोन से HTTPS पर चलते हों, जिनका टोकन आप खुद बना सकें और जो पढ़ तो सकें पर ट्रेड न कर सकें। इस वजह से फ़िलहाल ये बाहर हैं:

- **MetaTrader 4 और 5** खाते। इन्वेस्टर पासवर्ड केवल-पढ़ने वाला ऐक्सेस देता है, पर सिर्फ़ MetaTrader टर्मिनल के अंदर; ब्रोकर इसके लिए कोई HTTPS इंटरफ़ेस प्रकाशित नहीं करते।
- वे ब्रोकर जिनके API के लिए कंप्यूटर पर चलता प्रोग्राम चाहिए (जैसे Interactive Brokers का Client Portal Web API गेटवे; Capital इसकी जगह Flex Web Service इस्तेमाल करता है) या OAuth एप्लिकेशन का पंजीकरण।
- वे ब्रोकर जिनका API OAuth के ज़रिए केवल अल्पकालिक टोकन देता है, जैसे Saxo Bank (एक्सेस टोकन 20 मिनट चलते हैं; डेवलपर पोर्टल का 24-hour token केवल सिमुलेशन परिवेश के लिए काम करता है)।
- बिना सार्वजनिक API वाले बैंक और ब्रोकर।

इनमें से कई ब्रोकर [SnapTrade](#snaptrade) से कवर होते हैं। अन्यथा बैलेंस को **मैन्युअल** होल्डिंग के रूप में दर्ज करें और स्टेटमेंट देखने पर संख्या अपडेट करें। अगर आपका ब्रोकर खाते का मूल्य पढ़ने वाला सरल टोकन-आधारित HTTPS एंडपॉइंट देता है, तो उसके दस्तावेज़ के लिंक के साथ [इश्यू खोलें]({{ site.repo }}/issues)। Capital में हर ब्रोकर एक छोटा प्लगइन है; डेवलपर [प्लगइन गाइड]({{ site.repo }}/blob/main/BROKER-PLUGINS.md) का पालन करके नया जोड़ सकते हैं।

## संदेश और क्या करें {#messages}

| संदेश | क्या करें |
|---|---|
| *Interactive Brokers के लिए ब्रोकर स्क्रीन पर क्रेडेंशियल चाहिए* / *OANDA के लिए … क्रेडेंशियल चाहिए* | टोकन, कुंजी या क्लाइंट id को ब्रोकर टैब में क्रेडेंशियल के अंतर्गत पेस्ट करें। |
| *टोकन की अवधि समाप्त हो गई है; Client Portal में नया टोकन बनाएँ* | नया Flex Web Service टोकन बनाएँ और उसे पेस्ट करें। |
| *टोकन अमान्य है* | टोकन दोबारा कॉपी करें; नया टोकन पुराने की जगह लेता है। |
| *टोकन किसी दूसरे IP पते तक सीमित है* | टोकन IP प्रतिबंध के बिना बनाएँ। |
| *Flex Query id अमान्य है* | संख्या जाँचें; क्वेरी इसी लॉगिन की Activity Flex Query होनी चाहिए। |
| *Flex Query में Report Date और Total के साथ Net Asset Value (NAV) Summary in Base सेक्शन जोड़ें* | क्वेरी संपादित करें और सेक्शन तथा फ़ील्ड जोड़ें। |
| *Flex Query में Account Information का Currency फ़ील्ड जोड़ें* | क्वेरी संपादित करें और फ़ील्ड जोड़ें। |
| *क्वेरी ने N खाते लौटाए; हर खाते के लिए अलग Flex Query बनाएँ* | ऐसी क्वेरी बनाएँ जो एक खाते को कवर करे। |
| *स्टेटमेंट अभी तैयार नहीं है; एक मिनट बाद फिर रीफ़्रेश करें* | Interactive Brokers अभी रिपोर्ट बना रहा है; फिर रीफ़्रेश करें। |
| *पहुँच अस्वीकृत; प्रदाता की कुंजी या कोटा जाँचें* | OANDA का टोकन गलत है या रद्द कर दिया गया है, या Trading 212 की कुंजी-जोड़ी गलत है या उसमें Account data अनुमति नहीं है। |
| *SnapTrade के पास इस खाते का कुल मूल्य अभी नहीं है; कनेक्शन सिंक करें और फिर कोशिश करें* | SnapTrade ने ब्रोकरेज अभी सिंक नहीं किया है; बाद में फिर रीफ़्रेश करें। |
| *अभी कोई खाता जुड़ा नहीं है। पहले SnapTrade के ज़रिए ब्रोकरेज जोड़ें।* | एडिटर से Connection Portal खोलें और ब्रोकर जोड़ें। |
| *खाते का ऋणात्मक मान … समर्थित नहीं है* | खाता डेबिट में है; वह आपकी बचत में कुछ नहीं जोड़ता। |
| *tastytrade ने रीफ़्रेश टोकन या क्लाइंट सीक्रेट अस्वीकार कर दिया; नया ग्रांट बनाएँ* | एप्लिकेशन के लिए नया ग्रांट बनाएँ और उसका रीफ़्रेश टोकन पेस्ट करें; क्लाइंट सीक्रेट जाँचें। |
| *Capital.com ने सत्र नहीं खोला; API कुंजी, लॉगिन और कुंजी का पासवर्ड जाँचें* | कुंजी, ई-मेल या कुंजी का कस्टम पासवर्ड गलत है, या कुंजी की अवधि समाप्त हो गई है। |
| *खाता नहीं मिला; उसे फिर से चुनें* | ब्रोकर अब इस खाते को सूचीबद्ध नहीं करता; उसे संपादित करें और **खाते प्राप्त करें** से चुनें। |

इनमें से किसी के बाद पिछला मान दिखता रहता है, पुराना चिह्नित किया हुआ।
