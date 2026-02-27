package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson10DSL {
    fun get(): Lesson = Lesson(
        id = "dsl_builders",
        title = "🏗️ DSL и билдеры: Создание своих языков",
        description = "Мастер-класс по созданию предметно-ориентированных языков в Kotlin",
        difficulty = "advanced",
        duration = 45,
        topic = "dsl",
        theory = """
            # 🏗️ DSL (Domain Specific Language) в Kotlin

            **DSL (Domain-Specific Language)** — это язык программирования, специализированный для конкретной предметной области. Kotlin предоставляет уникальные возможности для создания **типобезопасных**, **выразительных** и **красивых** DSL прямо внутри языка, без внешних инструментов.

            ## 🎯 Зачем нужны DSL?

            DSL позволяют писать код, который выглядит как **декларативное описание**, а не как последовательность инструкций:

            ❌ **Обычный код** (императивный):
            ```kotlin
            val html = StringBuilder()
            html.append("<html>")
            html.append("<head><title>Моя страница</title></head>")
            html.append("<body><h1>Привет!</h1></body>")
            html.append("</html>")
            ```

            ✅ **DSL** (декларативный):
            ```kotlin
            html {
                head {
                    title("Моя страница")
                }
                body {
                    h1("Привет!")
                }
            }
            ```

            ## 🔑 Ключевые концепции Kotlin для создания DSL

            ### 1. Лямбды с получателем (Lambda with Receiver)

            Это **сердце** любого Kotlin DSL. Позволяет обращаться к методам объекта без явной квалификации.

            ```kotlin
            // Обычная лямбда
            val lambda: (StringBuilder) -> Unit = { sb -> sb.append("Hello") }
            
            // Лямбда с получателем (StringBuilder.() -> Unit)
            val lambdaWithReceiver: StringBuilder.() -> Unit = { append("Hello") }
            
            fun buildString(block: StringBuilder.() -> Unit): String {
                val sb = StringBuilder()
                sb.block() // Вызов лямбды с получателем
                return sb.toString()
            }
            
            val result = buildString {
                append("Hello")
                append(" ")
                append("World!")
            }
            println(result) // Hello World!
            ```

            ### 2. Инфиксные функции (Infix Functions)

            Позволяют вызывать функции без точки и скобок, создавая естественный язык:

            ```kotlin
            infix fun String.shouldBe(expected: String) = Assertion(this == expected)
            
            // Использование
            "Kotlin" shouldBe "Kotlin"  // Читается как предложение!
            ```

            ### 3. Перегрузка операторов (Operator Overloading)

            Позволяет использовать математические и другие операторы в DSL:

            ```kotlin
            operator fun String.unaryPlus() = println(this)  // +"Текст"
            operator fun Int.rangeTo(other: Int) = this..other  // 1..10
            ```

            ### 4. Функции расширения (Extension Functions)

            Добавляют новые методы к существующим классам:

            ```kotlin
            fun Table.tr(block: Tr.() -> Unit) { /* ... */ }
            fun Tr.td(text: String) { /* ... */ }
            ```

            ### 5. Аннотация @DslMarker

            Ограничивает область видимости, предотвращая "загрязнение" контекста:

            ```kotlin
            @DslMarker
            annotation class HtmlDsl
            
            @HtmlDsl
            class HTML { /* ... */ }
            
            @HtmlDsl
            class Body { /* ... */ }
            ```

            ## 🎨 Создаём свой первый DSL: HTML Builder

            ### Шаг 1: Базовые классы

            ```kotlin
            @DslMarker
            annotation class HtmlDsl
            
            @HtmlDsl
            class HTML {
                private val children = mutableListOf<HtmlElement>()
                
                fun head(init: Head.() -> Unit) {
                    val head = Head()
                    head.init()
                    children.add(head)
                }
                
                fun body(init: Body.() -> Unit) {
                    val body = Body()
                    body.init()
                    children.add(body)
                }
                
                override fun toString() = buildString {
                    append("<!DOCTYPE html>\n<html>\n")
                    children.forEach { append(it.toString()) }
                    append("</html>")
                }
            }
            
            @HtmlDsl
            class Head {
                private val elements = mutableListOf<String>()
                
                fun title(text: String) {
                    elements.add("<title>${'$'}text</title>")
                }
                
                fun meta(vararg attrs: Pair<String, String>) {
                    val attrsStr = attrs.joinToString(" ") { "${'$'}{it.first}=\"${'$'}{it.second}\"" }
                    elements.add("<meta ${'$'}attrsStr>")
                }
                
                override fun toString() = "<head>\n  ${'$'}{elements.joinToString("\n  ")}\n</head>"
            }
            
            @HtmlDsl
            class Body {
                private val elements = mutableListOf<String>()
                
                fun h1(text: String) = addTag("h1", text)
                fun p(text: String) = addTag("p", text)
                
                fun div(classes: String = "", init: Body.() -> Unit) {
                    val divBody = Body().apply(init)
                    elements.add("<div${'$'}{if (classes.isNotEmpty()) " class=\"${'$'}classes\"" else ""}>\n  ${'$'}{divBody}\n</div>")
                }
                
                private fun addTag(tag: String, text: String) {
                    elements.add("<${'$'}tag>${'$'}text</${'$'}tag>")
                }
                
                override fun toString() = elements.joinToString("\n")
            }
            
            fun html(init: HTML.() -> Unit): HTML = HTML().apply(init)
            ```

            ### Шаг 2: Использование

            ```kotlin
            val page = html {
                head {
                    title("Моя страница")
                    meta("charset" to "UTF-8")
                }
                body {
                    h1("Добро пожаловать!")
                    div("content") {
                        p("Это пример HTML DSL")
                    }
                }
            }
            ```

            ## 🧪 Реальные примеры DSL в экосистеме Kotlin

            ### 📦 Gradle Kotlin DSL

            ```kotlin
            plugins {
                kotlin("jvm") version "1.9.0"
                application
            }
            
            dependencies {
                implementation(kotlin("stdlib"))
                testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
            }
            
            tasks.test {
                useJUnitPlatform()
            }
            ```

            ### 🗄️ Kotlin SQL DSL (Exposed)

            ```kotlin
            object Users : Table() {
                val id = integer("id").autoIncrement()
                val name = varchar("name", 50)
                val age = integer("age")
            }
            
            // Запросы
            val adults = Users.select { Users.age greaterEq 18 }
            
            transaction {
                Users.insert {
                    it[name] = "Иван"
                    it[age] = 25
                }
            }
            ```

            ### 🎭 Kotlin Test DSL

            ```kotlin
            class MyTests : StringSpec({
                "длина строки должна быть 5" {
                    "hello".length shouldBe 5
                }
                
                "контейнер должен содержать элемент" {
                    listOf(1, 2, 3) shouldContain 2
                }
            })
            ```

            ## 🏗️ Продвинутые техники создания DSL

            ### 1. Контекстные объекты (Context Objects)

            ```kotlin
            class TransactionContext {
                fun commit() { println("Committing...") }
                fun rollback() { println("Rolling back...") }
            }
            
            fun transaction(block: TransactionContext.() -> Unit) {
                val context = TransactionContext()
                try {
                    context.block()
                    context.commit()
                } catch (e: Exception) {
                    context.rollback()
                }
            }
            
            // Использование
            transaction {
                // операции с БД
                commit() // или rollback()
            }
            ```

            ### 2. Вложенные контексты с @DslMarker

            ```kotlin
            @DslMarker
            annotation class QueryDsl
            
            @QueryDsl
            class SelectQuery {
                fun from(table: String) { /* ... */ }
                fun where(condition: String) { /* ... */ }
            }
            
            @QueryDsl
            class JoinContext {
                fun on(condition: String) { /* ... */ }
            }
            
            fun SelectQuery.join(table: String, init: JoinContext.() -> Unit) {
                val join = JoinContext()
                join.init()
                // обработка join
            }
            
            // Использование - нельзя вызвать join.on() вне join блока
            query {
                from("users")
                join("orders") {
                    on("users.id = orders.user_id")
                    // on() доступен только здесь
                }
            }
            ```

            ### 3. Инфиксные функции для естественного языка

            ```kotlin
            class ConditionBuilder {
                infix fun String.eq(value: Any): Condition = Condition(this, "=", value)
                infix fun String.gt(value: Number): Condition = Condition(this, ">", value)
                infix fun String.like(pattern: String): Condition = Condition(this, "LIKE", pattern)
            }
            
            // Использование
            where {
                "age" gt 18
                "name" eq "Иван"
                "email" like "%@gmail.com"
            }
            ```

            ### 4. Перегрузка операторов в DSL

            ```kotlin
            class RouteBuilder {
                private val routes = mutableListOf<String>()
                
                operator fun String.unaryPlus() {
                    routes.add(this)
                }
                
                fun build() = routes.joinToString("\n")
            }
            
            fun routes(block: RouteBuilder.() -> Unit): String {
                return RouteBuilder().apply(block).build()
            }
            
            // Использование
            val config = routes {
                +"GET /users -> getUsers()"
                +"POST /users -> createUser()"
                +"DELETE /users/:id -> deleteUser()"
            }
            ```

            ## 🎯 Типы DSL

            ### 1. Структурные DSL (как HTML)

            Строят иерархические структуры данных.

            ### 2. Конфигурационные DSL (как Gradle)

            Настраивают параметры и зависимости.

            ### 3. Поведенческие DSL (как тесты)

            Описывают сценарии и действия.

            ### 4. Языки запросов (как SQL DSL)

            Формируют запросы к данным.

            ## 📊 Сравнение подходов к созданию DSL

            | Подход | Сложность | Гибкость | Типобезопасность | Производительность |
            |--------|-----------|----------|------------------|-------------------|
            | Строковый (ручной парсинг) | 🔴 Высокая | 🟢 Высокая | 🔴 Низкая | 🟡 Средняя |
            | Аннотации + кодогенерация | 🟡 Средняя | 🟡 Средняя | 🟢 Высокая | 🟢 Высокая |
            | Kotlin DSL с лямбдами | 🟢 Низкая | 🟢 Высокая | 🟢 Высокая | 🟡 Средняя |
            | Type-safe builders | 🟢 Низкая | 🟡 Средняя | 🟢 Высокая | 🟢 Высокая |

            ## 💡 Best Practices

            ### ✅ Делайте DSL типобезопасным
            Используйте @DslMarker, чтобы предотвратить смешивание контекстов.

            ### ✅ Используйте говорящие имена функций
            `shouldBe`, `greaterThan`, `contains` — пусть код читается как английский.

            ### ✅ Предоставляйте понятные сообщения об ошибках
            ```kotlin
            require(condition) { "Понятное объяснение ошибки" }
            ```

            ### ✅ Документируйте ваш DSL
            ```kotlin
            /**
             * Начинает блок HTML-документа.
             * @param init лямбда с получателем HTML для построения страницы
             */
            fun html(init: HTML.() -> Unit): HTML
            ```

            ### ✅ Тестируйте DSL как обычный код
            ```kotlin
            @Test
            fun `html DSL должен создавать правильную структуру`() {
                val result = html {
                    body { h1("Test") }
                }.toString()
                
                assertTrue(result.contains("<h1>Test</h1>"))
            }
            ```

            ### ⚠️ Чего избегать
            - ❌ Слишком много магии (неявных преобразований)
            - ❌ Неоднозначных конструкций
            - ❌ Сложных правил, которые трудно объяснить
            - ❌ Игнорирования производительности

            ## 🚀 Пример: Полноценный SQL DSL

            Вот как может выглядеть реальный SQL DSL:

            ```kotlin
            val query = sql {
                select("users.name", "orders.total")
                from("users")
                join("orders") on "users.id = orders.user_id"
                where {
                    "users.age" greaterThan 18
                    and {
                        "orders.status" eq "completed"
                        or {
                            "orders.status" eq "pending"
                        }
                    }
                }
                orderBy("users.name" asc)
                limit(10)
                offset(20)
            }
            
            println(query.build())
            // SELECT users.name, orders.total 
            // FROM users 
            // JOIN orders ON users.id = orders.user_id 
            // WHERE users.age > 18 AND (orders.status = 'completed' OR orders.status = 'pending')
            // ORDER BY users.name ASC
            // LIMIT 10 OFFSET 20
            ```

            ## 🎓 Заключение

            Kotlin предоставляет уникальные возможности для создания **красивых**, **выразительных** и **типобезопасных** DSL. Используйте эту силу мудро:

            - Для **конфигурации** (Gradle, Ktor)
            - Для **построения структур** (HTML, XML)
            - Для **тестирования** (Kotest, Spek)
            - Для **запросов** (Exposed, Ktorm)
            - Для **маршрутизации** (Ktor, Spring)

            Правильно спроектированный DSL может значительно повысить читаемость и поддерживаемость кода, сделав его похожим на **декларативное описание**, а не на последовательность инструкций.
        """.trimIndent(),
        codeExample = """
            import kotlin.test.assertEquals
            import kotlin.test.assertTrue
            
            fun main() {
                println("🚀 Демонстрация DSL и билдеров в Kotlin")
                println("=".repeat(50))
                
                // ============================================================
                // 1. HTML DSL
                // ============================================================
                println("\n📄 1. HTML DSL")
                println("-".repeat(30))
                
                val htmlDoc = html {
                    head {
                        title("Моя страница")
                        meta("charset" to "UTF-8")
                        meta("viewport" to "width=device-width, initial-scale=1.0")
                    }
                    body {
                        h1("Добро пожаловать в Kotlin DSL!")
                        
                        div("content") {
                            p("Это HTML создан с помощью DSL на Kotlin")
                            p("Каждый элемент - это функция!")
                            
                            div("highlight") {
                                p("Вложенные структуры работают естественно")
                            }
                        }
                        
                        ul {
                            li("Первая причина: Читаемость")
                            li("Вторая причина: Типобезопасность")
                            li("Третья причина: Красота кода")
                        }
                        
                        div("footer") {
                            p("© 2024 Kotlin DSL Пример")
                        }
                    }
                }
                
                println("Сгенерированный HTML:")
                println(htmlDoc.toString().take(500) + "...")
                
                // ============================================================
                // 2. Person Builder DSL
                // ============================================================
                println("\n👤 2. Person Builder DSL")
                println("-".repeat(30))
                
                val person = person {
                    name = "Алексей Смирнов"
                    age = 28
                    
                    address {
                        city = "Санкт-Петербург"
                        street = "Невский проспект"
                        house = "15"
                        apartment = "42"
                    }
                    
                    contacts {
                        email = "alexey@example.com"
                        phone = "+7 (999) 123-45-67"
                        
                        social {
                            network = "GitHub"
                            handle = "alexey-dev"
                        }
                        
                        social {
                            network = "Telegram"
                            handle = "@alexey"
                        }
                    }
                    
                    job {
                        position = "Senior Kotlin Developer"
                        company = "TechCorp"
                        experience = 5
                        skills {
                            +"Kotlin"
                            +"Coroutines"
                            +"DSL Design"
                            +"Ktor"
                        }
                    }
                }
                
                println("Создан объект Person:")
                println("  Имя: ${'$'}{person.name}")
                println("  Возраст: ${'$'}{person.age}")
                println("  Адрес: ${'$'}{person.address}")
                println("  Email: ${'$'}{person.contacts.email}")
                println("  Должность: ${'$'}{person.job.position}")
                println("  Навыки: ${'$'}{person.job.skills.joinToString()}")
                
                // ============================================================
                // 3. SQL Query DSL
                // ============================================================
                println("\n🗄️ 3. SQL Query DSL")
                println("-".repeat(30))
                
                val query = sql {
                    select("u.id", "u.name", "COUNT(o.id) as order_count", "SUM(o.total) as total_spent")
                    from("users u")
                    
                    join("orders o") on "u.id = o.user_id"
                    
                    leftJoin("profiles p") on "u.id = p.user_id"
                    
                    where {
                        "u.age" greaterThan 21
                        
                        and {
                            "u.status" eq "active"
                            
                            or {
                                "p.verified" eq true
                                "o.total" greaterThan 1000
                            }
                        }
                    }
                    
                    groupBy("u.id", "u.name")
                    
                    having {
                        "COUNT(o.id)" greaterThan 5
                        "SUM(o.total)" greaterThan 5000
                    }
                    
                    orderBy("total_spent" desc)
                    orderBy("u.name" asc)
                    
                    limit(50)
                    offset(0)
                }
                
                println("Сгенерированный SQL запрос:")
                println(query.build())
                
                // ============================================================
                // 4. Configuration DSL
                // ============================================================
                println("\n⚙️ 4. Configuration DSL")
                println("-".repeat(30))
                
                val config = configure {
                    name("MyApp")
                    version("1.2.3")
                    
                    server {
                        host("localhost")
                        port(8080)
                        ssl(true)
                        
                        features {
                            +"cors"
                            +"compression"
                            +"metrics"
                        }
                    }
                    
                    database {
                        url("jdbc:postgresql://localhost:5432/mydb")
                        driver("org.postgresql.Driver")
                        
                        connectionPool {
                            minSize(5)
                            maxSize(20)
                            timeout(30000)
                        }
                        
                        migrations {
                            +"V1__init.sql"
                            +"V2__add_users.sql"
                            +"V3__add_orders.sql"
                        }
                    }
                    
                    logging {
                        level(LogLevel.DEBUG)
                        format(LogFormat.JSON)
                        
                        appenders {
                            +"console"
                            +"file"
                            +"elasticsearch"
                        }
                    }
                }
                
                println("Конфигурация:")
                println("  Application: ${'$'}{config.name} v${'$'}{config.version}")
                println("  Server: http://${'$'}{config.server.host}:${'$'}{config.server.port}")
                println("  Database: ${'$'}{config.database.url}")
                println("  Log level: ${'$'}{config.logging.level}")
                
                // ============================================================
                // 5. Test Suite DSL
                // ============================================================
                println("\n🧪 5. Test Suite DSL")
                println("-".repeat(30))
                
                val testResults = testSuite("Calculator Tests") {
                    beforeAll {
                        println("🔄 Инициализация тестового окружения...")
                    }
                    
                    afterAll {
                        println("🧹 Очистка после всех тестов...")
                    }
                    
                    test("сложение должно работать") {
                        val result = 2 + 2
                        assert(result == 4) { "2 + 2 должно быть 4" }
                        println("  ✓ Тест сложения пройден")
                    }
                    
                    test("вычитание должно работать") {
                        val result = 10 - 3
                        assert(result == 7) { "10 - 3 должно быть 7" }
                        println("  ✓ Тест вычитания пройден")
                    }
                    
                    group("Группа: Работа со строками") {
                        beforeEach {
                            println("    Подготовка перед тестом в группе...")
                        }
                        
                        test("конкатенация строк") {
                            val result = "Hello" + " " + "World"
                            assertEquals("Hello World", result)
                            println("    ✓ Тест конкатенации пройден")
                        }
                        
                        test("длина строки") {
                            val text = "Kotlin"
                            assertTrue(text.length == 6)
                            println("    ✓ Тест длины строки пройден")
                        }
                    }
                    
                    group("Группа: Работа с коллекциями") {
                        test("список содержит элементы") {
                            val list = listOf(1, 2, 3, 4, 5)
                            assert(list.contains(3)) { "Список должен содержать 3" }
                            assert(list.size == 5) { "Размер должен быть 5" }
                            println("    ✓ Тест списка пройден")
                        }
                        
                        test("множество уникальных элементов") {
                            val set = setOf(1, 2, 2, 3, 3, 3)
                            assert(set.size == 3) { "Дубликаты должны удаляться" }
                            println("    ✓ Тест множества пройден")
                        }
                    }
                }
                
                println("\nРезультаты тестов:")
                testResults.run()
                
                // ============================================================
                // 6. Routes DSL
                // ============================================================
                println("\n🛣️ 6. Routes DSL (с перегрузкой операторов)")
                println("-".repeat(30))
                
                val routes = routes {
                    +"GET    /users      → UserController.getAll()"
                    +"POST   /users      → UserController.create()"
                    +"GET    /users/:id  → UserController.getById()"
                    +"PUT    /users/:id  → UserController.update()"
                    +"DELETE /users/:id  → UserController.delete()"
                    
                    group("/api/v1") {
                        +"GET    /products   → ProductController.getAll()"
                        +"POST   /products   → ProductController.create()"
                        +"GET    /orders     → OrderController.getAll()"
                        
                        group("/admin") {
                            +"GET    /stats      → AdminController.stats()"
                            +"POST   /clear-cache → AdminController.clearCache()"
                        }
                    }
                }
                
                println("Маршруты приложения:")
                println(routes)
                
                // ============================================================
                // 7. UI Layout DSL (как в Jetpack Compose)
                // ============================================================
                println("\n🎨 7. UI Layout DSL")
                println("-".repeat(30))
                
                val ui = column {
                    padding(16)
                    
                    text("Привет, Kotlin DSL!") {
                        fontSize = 24
                        fontWeight = FontWeight.Bold
                        color = Color.BLUE
                    }
                    
                    spacer(height = 8)
                    
                    card {
                        padding(12)
                        elevation = 4
                        
                        column {
                            text("Заголовок карточки")
                            text("Описание с дополнительной информацией")
                            
                            row {
                                button("OK") {
                                    onClick { println("OK clicked!") }
                                }
                                
                                button("Cancel") {
                                    onClick { println("Cancel clicked!") }
                                }
                            }
                        }
                    }
                    
                    list(items = listOf("Первый", "Второй", "Третий")) { item ->
                        row {
                            icon(Icon.CHECK)
                            text(item)
                        }
                    }
                }
                
                println("UI структура:")
                println("  Тип: ${'$'}{ui::class.simpleName}")
                println("  Дочерних элементов: ${'$'}{ui.children.size}")
                
                println("\n" + "=".repeat(50))
                println("✅ Демонстрация завершена")
            }
            
            // =================================================================
            // РЕАЛИЗАЦИИ DSL
            // =================================================================
            
            // ========================= HTML DSL =============================
            @DslMarker
            annotation class HtmlDsl
            
            @HtmlDsl
            class HTML {
                private val children = mutableListOf<HtmlElement>()
                
                fun head(init: Head.() -> Unit) {
                    val head = Head()
                    head.init()
                    children.add(head)
                }
                
                fun body(init: Body.() -> Unit) {
                    val body = Body()
                    body.init()
                    children.add(body)
                }
                
                override fun toString() = buildString {
                    append("<!DOCTYPE html>\n<html>\n")
                    children.forEach { append(it.toString()) }
                    append("</html>")
                }
            }
            
            interface HtmlElement
            
            @HtmlDsl
            class Head : HtmlElement {
                private val elements = mutableListOf<String>()
                
                fun title(text: String) {
                    elements.add("  <title>${'$'}text</title>")
                }
                
                fun meta(vararg attrs: Pair<String, String>) {
                    val attrsStr = attrs.joinToString(" ") { "${'$'}{it.first}=\"${'$'}{it.second}\"" }
                    elements.add("  <meta ${'$'}attrsStr>")
                }
                
                override fun toString() = "<head>\n${'$'}{elements.joinToString("\n")}\n</head>\n"
            }
            
            @HtmlDsl
            class Body : HtmlElement {
                private val elements = mutableListOf<String>()
                
                fun h1(text: String) {
                    elements.add("  <h1>${'$'}text</h1>")
                }
                
                fun p(text: String) {
                    elements.add("  <p>${'$'}text</p>")
                }
                
                fun div(classes: String, init: Body.() -> Unit) {
                    val divBody = Body().apply(init)
                    elements.add("  <div class=\"${'$'}classes\">\n${'$'}{divBody}\n  </div>")
                }
                
                fun ul(init: Ul.() -> Unit) {
                    val ul = Ul().apply(init)
                    elements.add(ul.toString())
                }
                
                override fun toString() = elements.joinToString("\n") + "\n"
            }
            
            class Ul {
                private val items = mutableListOf<String>()
                
                fun li(text: String) {
                    items.add("    <li>${'$'}text</li>")
                }
                
                override fun toString() = "  <ul>\n${'$'}{items.joinToString("\n")}\n  </ul>"
            }
            
            fun html(init: HTML.() -> Unit): HTML = HTML().apply(init)
            
            // ========================= Person DSL =============================
            data class Person(
                val name: String,
                val age: Int,
                val address: Address,
                val contacts: Contacts,
                val job: Job
            )
            
            data class Address(
                val city: String,
                val street: String,
                val house: String,
                val apartment: String
            ) {
                override fun toString() = "${'$'}city, ${'$'}street ${'$'}house/${'$'}apartment"
            }
            
            data class Contacts(
                val email: String,
                val phone: String,
                val socials: List<Social>
            )
            
            data class Social(
                val network: String,
                val handle: String
            )
            
            data class Job(
                val position: String,
                val company: String,
                val experience: Int,
                val skills: List<String>
            )
            
            class PersonBuilder {
                var name: String = ""
                var age: Int = 0
                
                private val addressBuilder = AddressBuilder()
                private val contactsBuilder = ContactsBuilder()
                private val jobBuilder = JobBuilder()
                
                fun address(block: AddressBuilder.() -> Unit) {
                    addressBuilder.block()
                }
                
                fun contacts(block: ContactsBuilder.() -> Unit) {
                    contactsBuilder.block()
                }
                
                fun job(block: JobBuilder.() -> Unit) {
                    jobBuilder.block()
                }
                
                fun build() = Person(
                    name,
                    age,
                    addressBuilder.build(),
                    contactsBuilder.build(),
                    jobBuilder.build()
                )
            }
            
            class AddressBuilder {
                var city: String = ""
                var street: String = ""
                var house: String = ""
                var apartment: String = ""
                
                fun build() = Address(city, street, house, apartment)
            }
            
            class ContactsBuilder {
                var email: String = ""
                var phone: String = ""
                private val socials = mutableListOf<Social>()
                
                fun social(block: SocialBuilder.() -> Unit) {
                    val builder = SocialBuilder().apply(block)
                    socials.add(builder.build())
                }
                
                fun build() = Contacts(email, phone, socials)
            }
            
            class SocialBuilder {
                var network: String = ""
                var handle: String = ""
                
                fun build() = Social(network, handle)
            }
            
            class JobBuilder {
                var position: String = ""
                var company: String = ""
                var experience: Int = 0
                private val skills = mutableListOf<String>()
                
                fun skills(block: SkillsBuilder.() -> Unit) {
                    SkillsBuilder().apply(block).build().forEach { skills.add(it) }
                }
                
                fun build() = Job(position, company, experience, skills)
            }
            
            class SkillsBuilder {
                private val items = mutableListOf<String>()
                
                operator fun String.unaryPlus() {
                    items.add(this)
                }
                
                fun build() = items.toList()
            }
            
            fun person(block: PersonBuilder.() -> Unit): Person {
                return PersonBuilder().apply(block).build()
            }
            
            // ========================= SQL DSL =============================
            class SqlQueryBuilder {
                private val select = mutableListOf<String>()
                private val from = mutableListOf<String>()
                private val joins = mutableListOf<JoinClause>()
                private val whereConditions = mutableListOf<String>()
                private val groupByColumns = mutableListOf<String>()
                private val havingConditions = mutableListOf<String>()
                private val orderByColumns = mutableListOf<Pair<String, String>>()
                private var limitClause: Int? = null
                private var offsetClause: Int? = null
                
                fun select(vararg columns: String) {
                    select.addAll(columns)
                }
                
                fun from(table: String) {
                    from.clear()
                    from.add(table)
                }
                
                fun join(table: String, init: JoinBuilder.() -> Unit): SqlQueryBuilder {
                    val builder = JoinBuilder(table, "INNER JOIN").apply(init)
                    joins.add(builder.build())
                    return this
                }
                
                fun leftJoin(table: String, init: JoinBuilder.() -> Unit): SqlQueryBuilder {
                    val builder = JoinBuilder(table, "LEFT JOIN").apply(init)
                    joins.add(builder.build())
                    return this
                }
                
                infix fun String.on(condition: String) {
                    // используется в join блоках
                }
                
                fun where(init: WhereBuilder.() -> Unit) {
                    val builder = WhereBuilder().apply(init)
                    whereConditions.add(builder.build())
                }
                
                fun groupBy(vararg columns: String) {
                    groupByColumns.addAll(columns)
                }
                
                fun having(init: WhereBuilder.() -> Unit) {
                    val builder = WhereBuilder().apply(init)
                    havingConditions.add(builder.build())
                }
                
                infix fun String.desc(): Pair<String, String> = this to "DESC"
                infix fun String.asc(): Pair<String, String> = this to "ASC"
                
                fun orderBy(columnOrder: Pair<String, String>) {
                    orderByColumns.add(columnOrder)
                }
                
                fun orderBy(column: String, direction: String = "ASC") {
                    orderByColumns.add(column to direction)
                }
                
                fun limit(value: Int) {
                    limitClause = value
                }
                
                fun offset(value: Int) {
                    offsetClause = value
                }
                
                fun build(): String = buildString {
                    append("SELECT ")
                    append(if (select.isNotEmpty()) select.joinToString(", ") else "*")
                    
                    append("\nFROM ")
                    append(from.joinToString(", "))
                    
                    joins.forEach { join ->
                        append("\n${'$'}{join.type} ${'$'}{join.table} ON ${'$'}{join.condition}")
                    }
                    
                    if (whereConditions.isNotEmpty()) {
                        append("\nWHERE ")
                        append(whereConditions.joinToString(" AND "))
                    }
                    
                    if (groupByColumns.isNotEmpty()) {
                        append("\nGROUP BY ")
                        append(groupByColumns.joinToString(", "))
                    }
                    
                    if (havingConditions.isNotEmpty()) {
                        append("\nHAVING ")
                        append(havingConditions.joinToString(" AND "))
                    }
                    
                    if (orderByColumns.isNotEmpty()) {
                        append("\nORDER BY ")
                        append(orderByColumns.joinToString(", ") { "${'$'}{it.first} ${'$'}{it.second}" })
                    }
                    
                    limitClause?.let {
                        append("\nLIMIT ${'$'}it")
                    }
                    
                    offsetClause?.let {
                        append("\nOFFSET ${'$'}it")
                    }
                }
                
                data class JoinClause(val table: String, val type: String, val condition: String)
                
                inner class JoinBuilder(private val table: String, private val type: String) {
                    private var condition: String = ""
                    
                    infix fun String.on(cond: String) {
                        condition = cond
                    }
                    
                    fun build() = JoinClause(table, type, condition)
                }
                
                inner class WhereBuilder {
                    private val conditions = mutableListOf<String>()
                    
                    infix fun String.eq(value: Any): String {
                        val strValue = if (value is String) "'${'$'}value'" else "${'$'}value"
                        return "${'$'}this = ${'$'}strValue".also { conditions.add(it) }
                    }
                    
                    infix fun String.greaterThan(value: Number): String {
                        return "${'$'}this > ${'$'}value".also { conditions.add(it) }
                    }
                    
                    infix fun String.and(block: WhereBuilder.() -> String) {
                        val innerBuilder = WhereBuilder()
                        val innerCondition = innerBuilder.block()
                        conditions.add("(${'$'}innerCondition)")
                    }
                    
                    infix fun String.or(block: WhereBuilder.() -> String) {
                        val innerBuilder = WhereBuilder()
                        val innerCondition = innerBuilder.block()
                        conditions.add("(${'$'}innerCondition)")
                    }
                    
                    fun build(): String {
                        return if (conditions.size == 1) conditions.first()
                        else conditions.joinToString(" AND ")
                    }
                }
            }
            
            fun sql(init: SqlQueryBuilder.() -> Unit): SqlQueryBuilder {
                return SqlQueryBuilder().apply(init)
            }
            
            // ========================= Configuration DSL =============================
            data class AppConfig(
                val name: String,
                val version: String,
                val server: ServerConfig,
                val database: DatabaseConfig,
                val logging: LoggingConfig
            )
            
            data class ServerConfig(
                val host: String,
                val port: Int,
                val ssl: Boolean,
                val features: List<String>
            )
            
            data class DatabaseConfig(
                val url: String,
                val driver: String,
                val pool: PoolConfig,
                val migrations: List<String>
            )
            
            data class PoolConfig(
                val minSize: Int,
                val maxSize: Int,
                val timeout: Int
            )
            
            data class LoggingConfig(
                val level: LogLevel,
                val format: LogFormat,
                val appenders: List<String>
            )
            
            enum class LogLevel { DEBUG, INFO, WARN, ERROR }
            enum class LogFormat { TEXT, JSON, XML }
            
            class ConfigBuilder {
                var name: String = ""
                var version: String = ""
                val serverBuilder = ServerBuilder()
                val databaseBuilder = DatabaseBuilder()
                val loggingBuilder = LoggingBuilder()
                
                fun name(value: String) { name = value }
                fun version(value: String) { version = value }
                
                fun server(init: ServerBuilder.() -> Unit) {
                    serverBuilder.init()
                }
                
                fun database(init: DatabaseBuilder.() -> Unit) {
                    databaseBuilder.init()
                }
                
                fun logging(init: LoggingBuilder.() -> Unit) {
                    loggingBuilder.init()
                }
                
                fun build() = AppConfig(
                    name,
                    version,
                    serverBuilder.build(),
                    databaseBuilder.build(),
                    loggingBuilder.build()
                )
            }
            
            class ServerBuilder {
                var host: String = "localhost"
                var port: Int = 8080
                var ssl: Boolean = false
                val features = mutableListOf<String>()
                
                fun host(value: String) { host = value }
                fun port(value: Int) { port = value }
                fun ssl(value: Boolean) { ssl = value }
                
                fun features(init: FeaturesBuilder.() -> Unit) {
                    FeaturesBuilder().apply(init).build().forEach { features.add(it) }
                }
                
                fun build() = ServerConfig(host, port, ssl, features)
            }
            
            class DatabaseBuilder {
                var url: String = ""
                var driver: String = ""
                val poolBuilder = PoolBuilder()
                val migrations = mutableListOf<String>()
                
                fun url(value: String) { url = value }
                fun driver(value: String) { driver = value }
                
                fun connectionPool(init: PoolBuilder.() -> Unit) {
                    poolBuilder.init()
                }
                
                fun migrations(init: MigrationsBuilder.() -> Unit) {
                    MigrationsBuilder().apply(init).build().forEach { migrations.add(it) }
                }
                
                fun build() = DatabaseConfig(url, driver, poolBuilder.build(), migrations)
            }
            
            class PoolBuilder {
                var minSize: Int = 2
                var maxSize: Int = 10
                var timeout: Int = 30000
                
                fun minSize(value: Int) { minSize = value }
                fun maxSize(value: Int) { maxSize = value }
                fun timeout(value: Int) { timeout = value }
                
                fun build() = PoolConfig(minSize, maxSize, timeout)
            }
            
            class LoggingBuilder {
                var level: LogLevel = LogLevel.INFO
                var format: LogFormat = LogFormat.TEXT
                val appenders = mutableListOf<String>()
                
                fun level(value: LogLevel) { level = value }
                fun format(value: LogFormat) { format = value }
                
                fun appenders(init: AppendersBuilder.() -> Unit) {
                    AppendersBuilder().apply(init).build().forEach { appenders.add(it) }
                }
                
                fun build() = LoggingConfig(level, format, appenders)
            }
            
            class FeaturesBuilder {
                private val items = mutableListOf<String>()
                operator fun String.unaryPlus() { items.add(this) }
                fun build() = items.toList()
            }
            
            class MigrationsBuilder {
                private val items = mutableListOf<String>()
                operator fun String.unaryPlus() { items.add(this) }
                fun build() = items.toList()
            }
            
            class AppendersBuilder {
                private val items = mutableListOf<String>()
                operator fun String.unaryPlus() { items.add(this) }
                fun build() = items.toList()
            }
            
            fun configure(init: ConfigBuilder.() -> Unit): AppConfig {
                return ConfigBuilder().apply(init).build()
            }
            
            // ========================= Test Suite DSL =============================
            class TestSuite(val name: String) {
                private val beforeAllActions = mutableListOf<() -> Unit>()
                private val afterAllActions = mutableListOf<() -> Unit>()
                private val tests = mutableListOf<Test>()
                private val groups = mutableListOf<TestGroup>()
                
                fun beforeAll(block: () -> Unit) {
                    beforeAllActions.add(block)
                }
                
                fun afterAll(block: () -> Unit) {
                    afterAllActions.add(block)
                }
                
                fun test(name: String, block: () -> Unit) {
                    tests.add(Test(name, block))
                }
                
                fun group(name: String, block: TestGroup.() -> Unit) {
                    val group = TestGroup(name).apply(block)
                    groups.add(group)
                }
                
                fun run() {
                    beforeAllActions.forEach { it() }
                    
                    tests.forEach { it.run() }
                    groups.forEach { it.run() }
                    
                    afterAllActions.forEach { it() }
                }
            }
            
            class TestGroup(val name: String) {
                private val beforeEachActions = mutableListOf<() -> Unit>()
                private val tests = mutableListOf<Test>()
                
                fun beforeEach(block: () -> Unit) {
                    beforeEachActions.add(block)
                }
                
                fun test(name: String, block: () -> Unit) {
                    tests.add(Test(name, block))
                }
                
                fun run() {
                    println("  Группа: ${'$'}name")
                    tests.forEach { test ->
                        beforeEachActions.forEach { it() }
                        test.run()
                    }
                }
            }
            
            class Test(val name: String, val block: () -> Unit) {
                fun run() {
                    try {
                        block()
                    } catch (e: AssertionError) {
                        println("  ❌ ${'$'}name: провален - ${'$'}{e.message}")
                    }
                }
            }
            
            fun testSuite(name: String, block: TestSuite.() -> Unit): TestSuite {
                return TestSuite(name).apply(block)
            }
            
            // ========================= Routes DSL =============================
            class RoutesBuilder {
                private val routes = mutableListOf<String>()
                
                operator fun String.unaryPlus() {
                    routes.add(this)
                }
                
                fun group(prefix: String, init: RoutesBuilder.() -> Unit) {
                    val groupBuilder = RoutesBuilder()
                    groupBuilder.init()
                    groupBuilder.routes.forEach { route ->
                        routes.add("  ${'$'}route")
                    }
                }
                
                override fun toString() = routes.joinToString("\n")
            }
            
            fun routes(init: RoutesBuilder.() -> Unit): String {
                return RoutesBuilder().apply(init).toString()
            }
            
            // ========================= UI DSL =============================
            enum class Color { RED, GREEN, BLUE }
            enum class Icon { CHECK, WARNING, ERROR }
            enum class FontWeight { Normal, Bold }
            
            interface UIElement {
                val children: List<UIElement>
            }
            
            class Column : UIElement {
                override val children = mutableListOf<UIElement>()
                var padding: Int = 0
                
                fun padding(value: Int) { padding = value }
                
                fun text(text: String, init: Text.() -> Unit = {}) {
                    val textElement = Text(text).apply(init)
                    children.add(textElement)
                }
                
                fun spacer(height: Int) {
                    children.add(Spacer(height))
                }
                
                fun card(init: Card.() -> Unit) {
                    val cardElement = Card().apply(init)
                    children.add(cardElement)
                }
                
                fun row(init: Row.() -> Unit) {
                    val rowElement = Row().apply(init)
                    children.add(rowElement)
                }
                
                fun list(items: List<String>, itemBuilder: (String) -> Unit) {
                    items.forEach { itemBuilder(it) }
                }
            }
            
            class Row : UIElement {
                override val children = mutableListOf<UIElement>()
                
                fun icon(icon: Icon) {
                    children.add(IconElement(icon))
                }
                
                fun text(text: String) {
                    children.add(Text(text))
                }
                
                fun button(text: String, init: Button.() -> Unit) {
                    val buttonElement = Button(text).apply(init)
                    children.add(buttonElement)
                }
            }
            
            class Text(val text: String) : UIElement {
                override val children = emptyList<UIElement>()
                var fontSize: Int = 16
                var fontWeight: FontWeight = FontWeight.Normal
                var color: Color = Color.BLACK
            }
            
            class Spacer(val height: Int) : UIElement {
                override val children = emptyList<UIElement>()
            }
            
            class Card : UIElement {
                override val children = mutableListOf<UIElement>()
                var elevation: Int = 2
                var padding: Int = 0
                
                fun column(init: Column.() -> Unit) {
                    val columnElement = Column().apply(init)
                    children.add(columnElement)
                }
                
                fun text(text: String) {
                    children.add(Text(text))
                }
                
                fun row(init: Row.() -> Unit) {
                    val rowElement = Row().apply(init)
                    children.add(rowElement)
                }
            }
            
            class Button(val text: String) : UIElement {
                override val children = emptyList<UIElement>()
                var onClick: () -> Unit = {}
            }
            
            class IconElement(val icon: Icon) : UIElement {
                override val children = emptyList<UIElement>()
            }
            
            fun column(init: Column.() -> Unit): Column {
                return Column().apply(init)
            }
            
            fun row(init: Row.() -> Unit): Row {
                return Row().apply(init)
            }
        """.trimIndent()
    )
}