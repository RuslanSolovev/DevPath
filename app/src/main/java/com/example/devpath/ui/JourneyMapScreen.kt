package com.example.devpath.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class City(
    val name: String,
    val distanceFromMoscow: Int,
    val population: String,
    val area: String,
    val founded: String,
    val fact: String,
    val funFact1: String,
    val funFact2: String,
    val funFact3: String,
    val funFact4: String,
    val cuisine: String,
    val isMajor: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyMapScreen(
    navController: NavHostController,
    totalSteps: Int,
    stepLengthMeters: Double = 0.75
) {
    val cities = listOf(
        // Москва
        City("Москва", 0, "12,7 млн", "2 561 км²", "1147 г.",
            "🏛️ Сердце России. Москва — политический, экономический и культурный центр страны.",
            "Кремль — самая большая средневековая крепость в Европе, её стены протянулись на 2,2 км.",
            "В Москве 17 действующих вокзалов и 5 аэропортов, а также самое большое метро в Европе.",
            "Красная площадь — главная площадь страны, здесь находятся Храм Василия Блаженного и Мавзолей.",
            "Московский Кремль — резиденция президента РФ и объект Всемирного наследия ЮНЕСКО.",
            "Блины, пельмени, борщ", true),
        // Балашиха
        City("Балашиха", 22, "520 тыс.", "97 км²", "1830 г.",
            "🏭 Балашиха — крупнейший город-спутник Москвы, важный промышленный центр.",
            "В городе находится знаменитая усадьба Пехра-Яковлевское — образец русского классицизма.",
            "Балашихинский литейно-механический завод — одно из старейших предприятий региона.",
            "В Балашихе расположен крупнейший в Европе производственный комплекс Coca-Cola.",
            "Город активно застраивается новыми жилыми комплексами и парковыми зонами.",
            "", false),
        // Щёлково
        City("Щёлково", 35, "130 тыс.", "37 км²", "1925 г.",
            "🏭 Щёлково — текстильная столица региона, здесь работали крупнейшие мануфактуры XIX века.",
            "В городе находится Щёлковский историко-краеведческий музей с богатой коллекцией.",
            "Щёлковский район известен своими санаториями и домами отдыха на берегу Клязьмы.",
            "Здесь расположен аэродром Чкаловский — база военно-транспортной авиации России.",
            "В окрестностях города находится Медвежьи Озёра — популярное место отдыха москвичей.",
            "", false),
        // Фрязино
        City("Фрязино", 35, "60 тыс.", "9 км²", "1951 г.",
            "🔬 Фрязино — один из первых наукоградов России, центр радиоэлектроники и СВЧ-технологий.",
            "Здесь расположены ведущие НИИ в области электроники и космической связи.",
            "В городе находится единственный в России музей радиоэлектроники «Фрязино-наукоград».",
            "Фрязино — один из самых благоустроенных и компактных городов Подмосковья.",
            "В парке «Фрязино» проводятся ежегодные фестивали науки и техники.",
            "", false),
        // Сергиев Посад
        City("Сергиев Посад", 75, "100 тыс.", "50 км²", "1345 г.",
            "🏛️ Сергиев Посад — духовная столица России, центр православного паломничества.",
            "Троице-Сергиева Лавра — крупнейший мужской монастырь страны, объект ЮНЕСКО.",
            "В городе находится знаменитая Сергиево-Посадская игрушка — народный промысел.",
            "Здесь работал известный художник Михаил Нестеров, создавший цикл картин о св. Сергии.",
            "В городе проходят ежегодные ярмарки народных промыслов и фестивали колокольного звона.",
            "Сергиевские пряники", false),
        // Орехово-Зуево
        City("Орехово-Зуево", 95, "118 тыс.", "47 км²", "1917 г.",
            "🧵 Орехово-Зуево — родина Морозовской текстильной мануфактуры, крупнейшей в России XIX века.",
            "В городе находится Саввино-Сторожевский монастырь, основанный учеником Сергия Радонежского.",
            "Здесь родился и жил знаменитый поэт Николай Заболоцкий.",
            "В Орехово-Зуеве расположен уникальный мост через Клязьму — памятник инженерной мысли.",
            "Город известен своими традициями футбола — здесь базируется клуб «Знамя Труда».",
            "", false),
        // Владимир
        City("Владимир", 180, "350 тыс.", "308 км²", "990 г.",
            "🏰 Владимир — жемчужина Золотого кольца, древняя столица Северо-Восточной Руси.",
            "Золотые ворота, Успенский и Дмитриевский соборы — объекты Всемирного наследия ЮНЕСКО.",
            "В городе находится знаменитый Владимирский централ — тюрьма, известная по песне Михаила Круга.",
            "Здесь работали великие князья Андрей Боголюбский и Всеволод Большое Гнездо.",
            "Владимир славится своей вишнёвой настойкой и вишнёвыми садами.",
            "Владимирская вишня", false),
        // Муром
        City("Муром", 300, "110 тыс.", "44 км²", "862 г.",
            "🏰 Муром — родина былинного богатыря Ильи Муромца, мощи которого покоятся в Киево-Печерской лавре.",
            "Спасо-Преображенский монастырь — древнейший монастырь России, основанный в XI веке.",
            "В городе находится единственный в России памятник калачу — символу муромского гостеприимства.",
            "Муром — родина изобретателя телевидения Владимира Зворыкина.",
            "Каждое лето в Муроме проходит фестиваль «Муромское лето» с реконструкциями былинных боёв.",
            "Муромские калачи", false),
        // Нижний Новгород
        City("Нижний Новгород", 400, "1,2 млн", "460 км²", "1221 г.",
            "🌅 Нижний Новгород — столица закатов, здесь находится самая длинная лестница в России — Чкаловская (560 ступеней).",
            "Нижегородский кремль — неприступная крепость, которую не смогли взять татары.",
            "Город — родина изобретателя радио Александра Попова и конструктора Ростислава Алексеева.",
            "Здесь находится знаменитая Нижегородская ярмарка — одна из крупнейших в Европе.",
            "В городе расположен уникальный музей техники «ГАЗ» с коллекцией ретро-автомобилей.",
            "Нижегородский пряник", true),
        // Кстово
        City("Кстово", 440, "66 тыс.", "18 км²", "1957 г.",
            "🛢️ Кстово — центр нефтепереработки Нижегородской области, здесь находится НПЗ «Лукойл».",
            "Озеро Святое — место паломничества, по легенде здесь была явлена икона Божией Матери.",
            "В окрестностях города расположен Щёлковский хутор — историческая усадьба XVIII века.",
            "Кстово — город-спутник Нижнего Новгорода, активно развивающийся в последние годы.",
            "Здесь находится уникальный храм в честь Владимирской иконы Божией Матери.",
            "", false),
        // Шумерля
        City("Шумерля", 630, "30 тыс.", "13 км²", "1916 г.",
            "🚂 Шумерля — железнодорожный город, возникший как поселок при станции.",
            "Шумерля знаменита своими валенками — здесь производят знаменитые шумерлинские валенки.",
            "В городе есть единственный в Чувашии железнодорожный техникум.",
            "Шумерлинский завод специализируется на производстве оборудования для железной дороги.",
            "В окрестностях города находится заказник «Шумерлинский» с реликтовыми растениями.",
            "", false),
        // Чебоксары
        City("Чебоксары", 640, "497 тыс.", "233 км²", "1469 г.",
            "🌉 Чебоксары — жемчужина на Волге, столица Чувашской Республики.",
            "46-метровая статуя Мать-покровительница — главный символ города.",
            "Чебоксарский залив — рукотворное чудо, любимое место отдыха горожан.",
            "Здесь находится один из крупнейших тракторных заводов в России — «Промтрактор».",
            "Чебоксары — родина легендарного космонавта Андрияна Николаева.",
            "Чебоксарский хмель", false),
        // Казань
        City("Казань", 800, "1,3 млн", "425 км²", "1005 г.",
            "🕌 Казань — третья столица России, город, где встречаются христианство и ислам.",
            "Казанский Кремль — единственный татарский кремль в России, объект ЮНЕСКО.",
            "В городе находится самый большой в Европе цирк и знаменитое Казанское метро.",
            "Казань — родина Фёдора Шаляпина, Льва Толстого и Владимира Ленина.",
            "Казанский университет — один из старейших в России, здесь учился Лев Толстой.",
            "Эчпочмак, чак-чак", true),
        // Набережные Челны
        City("Набережные Челны", 1020, "545 тыс.", "171 км²", "1626 г.",
            "🚚 Набережные Челны — город грузовиков, здесь находится легендарный завод КАМАЗ.",
            "КАМАЗ — крупнейший в мире производитель большегрузных автомобилей.",
            "В городе работает единственный в России музей истории КАМАЗа.",
            "Набережные Челны — один из самых молодых городов-миллионников России.",
            "В городе расположен уникальный парк «Прибрежный» с живописной набережной.",
            "", false),
        // Пермь
        City("Пермь", 1380, "1,0 млн", "799 км²", "1723 г.",
            "🎭 Пермь — культурная столица Урала, здесь находится знаменитый Пермский театр оперы и балета.",
            "Пермская деревянная скульптура — уникальное явление, представленное в Пермской галерее.",
            "В городе есть памятник букве «Ё» — единственный в мире.",
            "Пермь — родина изобретателя радио Александр Попов провёл здесь свои первые опыты.",
            "Пермский период — геологический период, названный в честь города.",
            "Пермские пельмени", false),
        // Екатеринбург
        City("Екатеринбург", 1800, "1,5 млн", "1 111 км²", "1723 г.",
            "⛰️ Екатеринбург — столица Урала, город на границе Европы и Азии.",
            "Здесь находится памятник границе Европы и Азии, куда приезжают тысячи туристов.",
            "В городе расположен крупнейший в России музей изобразительных искусств за Уралом.",
            "Екатеринбург — родина Бориса Ельцина, первого президента России.",
            "В Екатеринбурге есть единственный в мире памятник клавиатуре и основателям города Татищеву и де Геннину.",
            "Уральские пельмени", true),
        // Тюмень
        City("Тюмень", 2100, "830 тыс.", "698 км²", "1586 г.",
            "🛢️ Тюмень — нефтяная столица России, первый русский город в Сибири.",
            "Здесь находится самый длинный мост в России — Мост Влюблённых через Туру.",
            "В Тюмени расположен единственный в мире памятник собакам-поводырям.",
            "Тюменский драматический театр — один из старейших за Уралом.",
            "Город славится своими термальными источниками и санаториями.",
            "", true),
        // Ишим
        City("Ишим", 2420, "67 тыс.", "46 км²", "1687 г.",
            "📖 Ишим — литературный город, родина автора «Конька-Горбунка» Петра Ершова.",
            "В городе находится единственный в мире памятник Коньку-Горбунку.",
            "Ишимский краеведческий музей — один из лучших на юге Тюменской области.",
            "Город стоит на Транссибирской магистрали, здесь останавливаются все поезда Москва-Владивосток.",
            "В Ишиме родился и жил знаменитый поэт и писатель Михаил Пришвин.",
            "", false),
        // Омск
        City("Омск", 2700, "1,1 млн", "572 км²", "1716 г.",
            "🏰 Омск — врата Сибири, здесь находится Омская крепость — памятник XVIII века.",
            "Омский драматический театр — один из старейших в России.",
            "В городе расположен крупнейший в Сибири Омский государственный технический университет.",
            "Здесь находится знаменитая Омская ТЭЦ-5 — один из крупнейших энергообъектов.",
            "Омск — родина актёра Михаила Ульянова и космонавта Владимира Комарова.",
            "", true),
        // Барабинск
        City("Барабинск", 3050, "30 тыс.", "44 км²", "1893 г.",
            "🚂 Барабинск — крупный железнодорожный узел на Транссибе.",
            "Здесь был построен знаменитый самолёт «Ан-2» — «Кукурузник».",
            "Барабинск — родина дважды Героя Советского Союза Александра Клубова.",
            "В городе находится единственный в Новосибирской области памятник паровозу.",
            "Барабинская степь — уникальный природный заповедник с редкими птицами.",
            "", false),
        // Новосибирск
        City("Новосибирск", 3400, "1,6 млн", "502 км²", "1893 г.",
            "🎭 Новосибирск — культурная столица Сибири, здесь находится самый большой театр в России — Новосибирский оперный.",
            "Мост через Обь, построенный для Транссиба, был первым в Сибири.",
            "В городе расположен Академгородок — мировой центр науки.",
            "Новосибирск стоит на реке Обь — одной из крупнейших рек мира.",
            "Новосибирский зоопарк — один из лучших в мире по сохранению редких видов животных.",
            "", true),
        // Томск
        City("Томск", 3570, "576 тыс.", "294 км²", "1604 г.",
            "🎓 Томск — старейший университетский город за Уралом, здесь находится Томский государственный университет — первый в Сибири.",
            "В городе сохранилось более 100 памятников деревянного зодчества — «деревянное кружево».",
            "Томск — один из центров российской атомной промышленности.",
            "Здесь родился известный писатель и сатирик Михаил Зощенко.",
            "Томские учёные создают уникальные лекарства и нанотехнологии.",
            "", false),
        // Кемерово
        City("Кемерово", 3700, "557 тыс.", "282 км²", "1918 г.",
            "⛏️ Кемерово — столица Кузбасса, угольная столица России.",
            "Кемеровский мост через Томь — один из самых длинных в Сибири.",
            "В городе находится знаменитый Кузбасский ботанический сад с редкими растениями.",
            "Кемерово — родина легендарного хоккеиста Сергея Бобровского.",
            "Здесь расположен один из крупнейших в России угольных разрезов «Кедровский».",
            "", false),
        // Красноярск
        City("Красноярск", 4100, "1,1 млн", "348 км²", "1628 г.",
            "🌉 Красноярск — один из самых красивых городов Сибири на Енисее.",
            "Вантовый мост через Енисей — один из самых длинных в мире (длина пролёта 485 метров).",
            "Красноярские Столбы — национальный парк с уникальными скалами, известный скалолазный центр.",
            "Многие знаменитые актёры и певцы родом из Красноярска.",
            "Красноярская ГЭС — одна из крупнейших гидроэлектростанций в мире.",
            "", true),
        // Тайшет
        City("Тайшет", 4510, "34 тыс.", "40 км²", "1897 г.",
            "🚂 Тайшет — важный железнодорожный узел Байкало-Амурской магистрали (БАМ).",
            "Здесь находится станция Тайшет — начальная точка БАМа.",
            "Поэт Евгений Евтушенко в поэме «Братская ГЭС» упоминал Тайшет.",
            "В окрестностях города расположен крупный лесопромышленный комплекс.",
            "Тайшет — центр алюминиевой промышленности, здесь строится крупнейший завод.",
            "", false),
        // Иркутск
        City("Иркутск", 5100, "617 тыс.", "277 км²", "1661 г.",
            "💎 Иркутск — ворота Байкала, купеческий город с деревянным кружевом.",
            "В городе находится более 500 памятников архитектуры, многие из них — деревянные кружевные дома.",
            "Знаменский монастырь — место упокоения жён декабристов.",
            "Исторический центр — уникальный образец сибирского барокко.",
            "Иркутское водохранилище — одно из самых больших в России.",
            "Байкальский омуль", true),
        // Улан-Удэ
        City("Улан-Удэ", 5600, "435 тыс.", "347 км²", "1666 г.",
            "🗿 Улан-Удэ — буддийская столица России, здесь находится Иволгинский дацан.",
            "Самый большой памятник Ленину в мире — голова 7,7 метра, вес 42 тонны.",
            "Город расположен в живописной долине реки Уды и Селенги.",
            "Здесь работает крупнейший в России авиационный завод («Улан-Удэнский авиационный завод»).",
            "Этнографический музей народов Забайкалья — один из крупнейших в России.",
            "Позы (буузы)", true),
        // Чита
        City("Чита", 6200, "350 тыс.", "538 км²", "1653 г.",
            "⛰️ Чита — столица Забайкалья, важный железнодорожный узел.",
            "Декабристы, сосланные в Читу, оставили глубокий след в истории города — здесь есть церковь декабристов.",
            "Читинский дацан — один из старейших буддийских храмов в России.",
            "В городе находится уникальный минеральный источник с горячей водой.",
            "Центр города — образец забайкальского зодчества XVIII–XIX веков.",
            "", true),
        // Нерчинск
        City("Нерчинск", 6600, "15 тыс.", "24 км²", "1653 г.",
            "🏰 Нерчинск — один из первых русских острогов в Забайкалье.",
            "Здесь был сослан протопоп Аввакум, один из духовных лидеров старообрядчества.",
            "Нерчинский краеведческий музей — один из старейших в Забайкалье.",
            "В городе находилось управление Нерчинской каторгой.",
            "Нерчинские рудники — место каторги декабристов.",
            "", false),
        // Свободный
        City("Свободный", 7680, "54 тыс.", "58 км²", "1912 г.",
            "🚀 Свободный — космическая гавань России, здесь строится космодром «Восточный».",
            "Свободный был переименован из Алексеевска в честь революции.",
            "Город знаменит своим драмтеатром и музыкальным училищем.",
            "Свободненский район — один из крупнейших сельскохозяйственных районов области.",
            "Здесь добывают золото, лес и строительные материалы.",
            "", false),
        // Белогорск
        City("Белогорск", 7950, "65 тыс.", "39 км²", "1860 г.",
            "🛩️ Белогорск — центр штурмовой авиации Дальнего Востока.",
            "Здесь есть замечательный музей авиации под открытым небом.",
            "Станция Белогорск — важный железнодорожный узел на Транссибе.",
            "Близ города — знаменитые санатории и источники.",
            "Белогорский район — крупный сельскохозяйственный центр.",
            "", false),
        // Хабаровск
        City("Хабаровск", 8500, "617 тыс.", "386 км²", "1858 г.",
            "🐅 Хабаровск — столица Дальнего Востока, величественный город на высоких берегах Амура.",
            "Символ города — тигр, а также знаменитый Утёс.",
            "Амурский мост — единственная переправа через Амур на Транссибе.",
            "Краеведческий музей — один из богатейших в России.",
            "Хабаровск — центр деревянного зодчества и купечества.",
            "", true),
        // Уссурийск
        City("Уссурийск", 9140, "173 тыс.", "173 км²", "1866 г.",
            "🌸 Уссурийск — город цветов и живая легенда Дальнего Востока.",
            "В городе находится уникальный природный заповедник «Кедровая Падь».",
            "Уссурийская тайга — дом для дальневосточного леопарда.",
            "В Уссурийске сохранилась архитектура начала XX века.",
            "Город — центр виноградарства, садоводства и паркового искусства.",
            "", false),
        // Владивосток
        City("Владивосток", 9300, "605 тыс.", "331 км²", "1860 г.",
            "🌊 Владивосток — конечная точка Транссиба, город у самого океана.",
            "Мост на Русский остров — один из самых высоких в мире (324 метра).",
            "Золотой мост и бухта Золотой Рог — визитная карточка города.",
            "В городе находится штаб Тихоокеанского флота России.",
            "Владивосток — крупнейший порт России на Дальнем Востоке.",
            "Морепродукты, крабы", true)
    )

    val totalDistance = 9300
    val walkedDistanceKm = (totalSteps * stepLengthMeters) / 1000.0
    val progress = (walkedDistanceKm / totalDistance).coerceIn(0.0, 1.0)

    val animatedProgress by animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = tween(1000),
        label = "progress"
    )

    val currentCity = cities.lastOrNull { it.distanceFromMoscow <= walkedDistanceKm } ?: cities.first()
    val currentCityIndex = cities.indexOf(currentCity)
    val nextCity = cities.getOrNull(currentCityIndex + 1) ?: cities.last()

    val walkedFromMoscow = walkedDistanceKm.coerceAtMost(if (nextCity != currentCity) nextCity.distanceFromMoscow.toDouble() else totalDistance.toDouble())
    val walkedFromCurrentToNext = (walkedFromMoscow - currentCity.distanceFromMoscow).coerceAtLeast(0.0)
    val distanceToNextCity = if (nextCity != currentCity) {
        (nextCity.distanceFromMoscow - walkedDistanceKm).coerceAtLeast(0.0)
    } else 0.0

    val progressToNext = if (nextCity != currentCity && nextCity.distanceFromMoscow > currentCity.distanceFromMoscow) {
        (walkedFromCurrentToNext / (nextCity.distanceFromMoscow - currentCity.distanceFromMoscow)).coerceIn(0.0, 1.0)
    } else 0.0

    val formattedProgress = String.format("%.3f", progress * 100) + "%"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A1A))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Главная карточка с прогрессом
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A2E)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = Color(0xFFFF6B6B)
                                )
                            }
                            Text(
                                "Путь к Владивостоку",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(40.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = formattedProgress,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = Color(0xFFFF6B6B),
                            trackColor = Color(0xFF2D2D44)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("👣 Пройдено шагов", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8888AA))
                                Text(
                                    formatNumber(totalSteps),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("📏 Пройдено км", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8888AA))
                                Text(
                                    formatDistance(walkedDistanceKm),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("🎯 Осталось км", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8888AA))
                                Text(
                                    formatDistance(totalDistance - walkedDistanceKm),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("📊 Прогресс", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8888AA))
                                Text(
                                    String.format("%.3f", progress * 100) + "%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                        }
                    }
                }
            }

            // Карточка текущего города
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF16213E)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("📍 ТЕКУЩАЯ ОСТАНОВКА", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B6B))
                                Text(
                                    currentCity.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "${formatDistance(walkedFromMoscow)} км от Москвы",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = Color(0xFFFF6B6B).copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFF2D2D44))
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Основной факт
                            Row(verticalAlignment = Alignment.Top) {
                                Text("📜 ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
                                Text(currentCity.fact, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                            }
                            // Дополнительные факты
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✨ ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
                                Text(currentCity.funFact1, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✨ ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
                                Text(currentCity.funFact2, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✨ ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
                                Text(currentCity.funFact3, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("✨ ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
                                Text(currentCity.funFact4, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                            }
                            if (currentCity.cuisine.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("🍽️ ", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B6B))
                                    Text("Что попробовать: ${currentCity.cuisine}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                                }
                            }
                        }
                    }
                }
            }

            // Карточка следующего города
            if (nextCity != currentCity) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F0F1A)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("🎯 СЛЕДУЮЩАЯ ОСТАНОВКА", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8888AA))
                                    Text(
                                        nextCity.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "${formatDistance(distanceToNextCity)} км осталось (${nextCity.distanceFromMoscow} км от Москвы)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF6B6B)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Outlined.DirectionsRun,
                                            contentDescription = null,
                                            tint = Color(0xFF4A90E2),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = progressToNext.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = Color(0xFFFF6B6B),
                                trackColor = Color(0xFF2D2D44)
                            )
                            Text(
                                "${String.format("%.1f", progressToNext * 100)}% пути до ${nextCity.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8888AA),
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            // Таймлайн городов
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F0F1A)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🗺️ МАРШРУТ • ${cities.size} ГОРОДОВ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF6B6B),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )

                        cities.forEachIndexed { index, city ->
                            val isReached = city.distanceFromMoscow <= walkedDistanceKm
                            val isLast = index == cities.lastIndex
                            val isMajor = city.isMajor
                            var expanded by remember { mutableStateOf(false) }
                            var showFullInfoDialog by remember { mutableStateOf(false) }

                            // Диалог с полной информацией о городе
                            if (showFullInfoDialog) {
                                AlertDialog(
                                    onDismissRequest = { showFullInfoDialog = false },
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                city.name,
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF6B6B)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "(${city.distanceFromMoscow} км)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFFFF6B6B)
                                            )
                                        }
                                    },
                                    text = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 400.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Основная информация
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFF16213E)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text("📊 ОСНОВНАЯ ИНФОРМАЦИЯ", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B6B))
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("👥 Население: ${city.population}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                                    Text("🗺️ Площадь: ${city.area}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                                    Text("📅 Основан: ${city.founded}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                                }
                                            }

                                            // История
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFF16213E)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text("📜 ИСТОРИЯ", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B6B))
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(city.fact, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                                                }
                                            }

                                            // Интересные факты
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFF16213E)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text("✨ ИНТЕРЕСНЫЕ ФАКТЫ", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B6B))
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("• ${city.funFact1}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                                                    Text("• ${city.funFact2}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                                                    Text("• ${city.funFact3}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                                                    Text("• ${city.funFact4}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                                                }
                                            }

                                            // Кухня
                                            if (city.cuisine.isNotEmpty()) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFF16213E)
                                                    )
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text("🍽️ МЕСТНАЯ КУХНЯ", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B6B))
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(city.cuisine, style = MaterialTheme.typography.bodySmall, color = Color(0xFFAAAAAA))
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showFullInfoDialog = false }) {
                                            Text("Закрыть", color = Color(0xFFFF6B6B))
                                        }
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    containerColor = Color(0xFF0A0A1A)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isMajor) 24.dp else 18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isReached) Color(0xFFFF6B6B)
                                                else if (isMajor) Color(0xFF2D2D44)
                                                else Color(0xFF1A1A2E)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isReached && isMajor) {
                                            Icon(
                                                Icons.Outlined.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = Color.White
                                            )
                                        } else if (!isReached && isMajor) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF6B6B))
                                            )
                                        }
                                    }

                                    if (!isLast) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(if (expanded) 100.dp else 40.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (isReached) Color(0xFFFF6B6B).copy(alpha = 0.5f)
                                                    else Color(0xFF2D2D44)
                                                )
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp, bottom = 6.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            // При клике на весь город открываем диалог с информацией
                                            showFullInfoDialog = true
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isReached)
                                            Color(0xFFFF6B6B).copy(alpha = 0.12f)
                                        else if (isMajor)
                                            Color(0xFF16213E)
                                        else
                                            Color(0xFF0F0F1A)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isReached) 2.dp else 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                city.name,
                                                style = if (isMajor)
                                                    MaterialTheme.typography.titleSmall
                                                else
                                                    MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isReached) FontWeight.Bold else if (isMajor) FontWeight.Medium else FontWeight.Normal,
                                                color = if (isReached) Color(0xFFFF6B6B) else Color.White
                                            )
                                            if (isReached && isMajor) {
                                                Icon(
                                                    Icons.Outlined.Done,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFFFF6B6B)
                                                )
                                            }
                                        }
                                        Text(
                                            "${city.distanceFromMoscow} км",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF8888AA)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "«Дорога в тысячу миль начинается с одного шага» — Лао-Цзы",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8888AA),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

private fun formatNumber(number: Int): String {
    return String.format("%,d", number).replace(',', ' ')
}

private fun formatDistance(distance: Double): String {
    return String.format("%.3f", distance).replace(',', '.')
}