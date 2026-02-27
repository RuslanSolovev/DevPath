package com.example.devpath.data.repository.lessons

import com.example.devpath.domain.models.Lesson

object Lesson06OOP {
    fun get(): Lesson = Lesson(
        id = "oop_kotlin",
        title = "🏭 Объектно-ориентированное программирование в Kotlin",
        description = "Классы, объекты, наследование, интерфейсы и все принципы ООП",
        difficulty = "intermediate",
        duration = 45,
        topic = "classes",
        theory = """
            # 🏭 Объектно-ориентированное программирование в Kotlin

            Kotlin — это мультипарадигмальный язык, который полностью поддерживает объектно-ориентированное программирование (ООП), но при этом добавляет множество современных улучшений по сравнению с традиционными ООП-языками, такими как Java.

            ## 🎯 Основные принципы ООП

            | Принцип | Описание |
            |---------|----------|
            | **Инкапсуляция** | Скрытие внутренней реализации, доступ через методы |
            | **Наследование** | Создание новых классов на основе существующих |
            | **Полиморфизм** | Один интерфейс — множество реализаций |
            | **Абстракция** | Выделение существенных характеристик объекта |

            ## 📦 Классы и объекты

            ### Объявление простого класса

            ```kotlin
            class Person {
                // Свойства (поля)
                var name: String = ""
                var age: Int = 0
                
                // Методы
                fun speak() {
                    println("Привет, меня зовут ${'$'}name")
                }
                
                fun haveBirthday() {
                    age++
                    println("С днём рождения! Теперь мне ${'$'}age лет")
                }
            }
            
            // Использование
            val person = Person()
            person.name = "Алексей"
            person.age = 25
            person.speak()
            person.haveBirthday()
            ```

            ### Конструкторы

            Kotlin предлагает два типа конструкторов: **первичный** (primary) и **вторичные** (secondary).

            #### Первичный конструктор

            Первичный конструктор объявляется прямо в заголовке класса:

            ```kotlin
            class Person(firstName: String, lastName: String) {
                val fullName = "${'$'}firstName ${'$'}lastName"
                
                init {
                    println("Создан человек: ${'$'}fullName")
                }
            }
            ```

            Свойства можно объявить прямо в конструкторе, используя `val` или `var`:

            ```kotlin
            class Person(
                val firstName: String,  // свойство класса
                val lastName: String,   // свойство класса
                age: Int                 // параметр конструктора (не свойство)
            ) {
                var age: Int = age        // свойство, инициализированное параметром
                    set(value) {
                        if (value > 0) field = value
                    }
                
                val fullName: String = "${'$'}firstName ${'$'}lastName"
                
                init {
                    println("Создан человек: ${'$'}fullName, возраст: ${'$'}age")
                }
            }
            ```

            #### Вторичные конструкторы

            Иногда нужно несколько способов создания объекта:

            ```kotlin
            class Person {
                var name: String
                var age: Int
                
                // Первичный конструктор через this
                constructor(name: String) {
                    this.name = name
                    this.age = 0
                }
                
                constructor(name: String, age: Int) {
                    this.name = name
                    this.age = age
                }
                
                // Делегирование конструктора
                constructor() : this("Аноним") {
                    println("Создан анонимный пользователь")
                }
            }
            
            // Использование
            val p1 = Person("Алексей")
            val p2 = Person("Мария", 25)
            val p3 = Person()
            ```

            ### Модификаторы доступа

            | Модификатор | Доступ |
            |-------------|--------|
            | `public` (по умолчанию) | Виден везде |
            | `private` | Виден только внутри класса |
            | `protected` | Виден в классе и наследниках |
            | `internal` | Виден внутри модуля |

            ```kotlin
            class BankAccount {
                private var balance: Double = 0.0  // доступно только внутри класса
                internal val accountNumber: String = generateNumber()  // доступно в модуле
                
                fun deposit(amount: Double) {
                    if (amount > 0) {
                        balance += amount
                        println("Внесено ${'$'}amount. Баланс: ${'$'}balance")
                    }
                }
                
                fun getBalance(): Double = balance  // публичный метод доступа
            }
            ```

            ## 👨‍👦 Наследование

            В Kotlin все классы по умолчанию **final** (не могут быть унаследованы). Чтобы разрешить наследование, нужно пометить класс ключевым словом `open`.

            ### Базовый класс и наследник

            ```kotlin
            open class Animal(val name: String) {
                open fun makeSound() {
                    println("Животное издает звук")
                }
                
                fun sleep() {
                    println("${'$'}name спит")
                }
            }
            
            class Dog(name: String, val breed: String) : Animal(name) {
                override fun makeSound() {
                    println("${'$'}name (${'$'}breed) говорит: Гав-гав!")
                }
                
                fun fetch() {
                    println("${'$'}name приносит палку")
                }
            }
            
            class Cat(name: String) : Animal(name) {
                override fun makeSound() {
                    println("${'$'}name говорит: Мяу!")
                }
            }
            ```

            ### Вызов методов родителя

            ```kotlin
            open class Vehicle(val brand: String) {
                open fun start() {
                    println("Запуск двигателя ${'$'}brand")
                }
            }
            
            class Car(brand: String, val model: String) : Vehicle(brand) {
                override fun start() {
                    super.start()  // вызов метода родителя
                    println("Автомобиль ${'$'}brand ${'$'}model готов к поездке")
                }
            }
            ```

            ### Абстрактные классы

            Абстрактные классы не могут быть instantiated и могут содержать абстрактные методы (без реализации).

            ```kotlin
            abstract class Shape {
                abstract fun area(): Double
                abstract fun perimeter(): Double
                
                fun description() {
                    println("Я фигура с площадью ${'$'}{area()} и периметром ${'$'}{perimeter()}")
                }
            }
            
            class Circle(val radius: Double) : Shape() {
                override fun area(): Double = Math.PI * radius * radius
                override fun perimeter(): Double = 2 * Math.PI * radius
            }
            
            class Rectangle(val width: Double, val height: Double) : Shape() {
                override fun area(): Double = width * height
                override fun perimeter(): Double = 2 * (width + height)
            }
            ```

            ## 🔌 Интерфейсы

            Интерфейсы определяют контракт, который класс должен реализовать. В Kotlin интерфейсы могут содержать реализацию методов по умолчанию.

            ```kotlin
            interface Drawable {
                fun draw()
                
                // Метод с реализацией по умолчанию
                fun drawOutline() {
                    println("Рисую контур")
                }
            }
            
            interface Movable {
                fun move(dx: Int, dy: Int)
                
                // Свойства в интерфейсах
                val speed: Int
                    get() = 10  // константа по умолчанию
            }
            
            class Rectangle(val width: Int, val height: Int) : Drawable, Movable {
                override fun draw() {
                    println("Рисую прямоугольник ${'$'}width x ${'$'}height")
                }
                
                override fun move(dx: Int, dy: Int) {
                 println("🏃 Перемещаю на (${'$'}dx, ${'$'}dy) со скоростью ${'$'}speed")
                }
                
                // Можно переопределить свойство
                override val speed: Int = 20
            }
            ```

            ### Множественное наследование интерфейсов

            ```kotlin
            interface A {
                fun foo() { println("A.foo()") }
                fun bar()
            }
            
            interface B {
                fun foo() { println("B.foo()") }
                fun baz() { println("B.baz()") }
            }
            
            class C : A, B {
                override fun foo() {
                    super<A>.foo()  // вызов конкретной реализации
                    super<B>.foo()
                    println("C.foo()")
                }
                
                override fun bar() {
                    println("C.bar()")
                }
                
                // baz() наследуется от B
            }
            ```

            ## 📊 Data классы

            Data классы — одна из самых полезных фич Kotlin. Они автоматически генерируют `equals()`, `hashCode()`, `toString()`, `copy()` и функции `componentN()`.

            ```kotlin
            data class User(
                val id: Int,
                val name: String,
                val email: String,
                val isActive: Boolean = true
            )
            
            fun main() {
                val user1 = User(1, "Иван", "ivan@example.com")
                val user2 = User(1, "Иван", "ivan@example.com")
                val user3 = user1.copy(name = "Петр")
                
                println(user1)  // автоматический toString()
                println("user1 == user2: ${'$'}{user1 == user2}")  // true
                
                // Деструктуризация
                val (id, name, email) = user1
                println("ID: ${'$'}id, Имя: ${'$'}name, Email: ${'$'}email")
                
                // Копирование с изменениями
                val updated = user1.copy(email = "ivan@newmail.com", isActive = false)
            }
            ```

            ### Требования к data классам:
            - Первичный конструктор должен иметь минимум один параметр
            - Все параметры должны быть помечены `val` или `var`
            - Не могут быть abstract, open, sealed или inner

            ## 🎨 Enum классы

            Enum классы представляют ограниченный набор констант.

            ```kotlin
            enum class Status {
                ACTIVE, INACTIVE, PENDING, BLOCKED
            }
            
            enum class Color(val rgb: Int) {
                RED(0xFF0000),
                GREEN(0x00FF00),
                BLUE(0x0000FF),
                YELLOW(0xFFFF00);
                
                fun containsRed(): Boolean = (rgb and 0xFF0000) != 0
                
                fun description(): String = when (this) {
                    RED -> "Красный"
                    GREEN -> "Зеленый"
                    BLUE -> "Синий"
                    YELLOW -> "Желтый"
                }
            }
            
            fun processStatus(status: Status) {
                when (status) {
                    Status.ACTIVE -> println("Пользователь активен")
                    Status.INACTIVE -> println("Пользователь неактивен")
                    Status.PENDING -> println("Ожидает подтверждения")
                    Status.BLOCKED -> println("Пользователь заблокирован")
                }
            }
            ```

            ### Enum с анонимными классами

            ```kotlin
            enum class Operation {
                PLUS {
                    override fun apply(x: Int, y: Int): Int = x + y
                },
                MINUS {
                    override fun apply(x: Int, y: Int): Int = x - y
                },
                MULTIPLY {
                    override fun apply(x: Int, y: Int): Int = x * y
                };
                
                abstract fun apply(x: Int, y: Int): Int
            }
            ```

            ## 🏛️ Объекты и синглтоны

            ### Object declaration (синглтон)

            Ключевое слово `object` создаёт **синглтон** — класс, который имеет ровно один экземпляр.

            ```kotlin
            object DatabaseConfig {
                const val URL = "jdbc:mysql://localhost:3306/mydb"
                const val USER = "root"
                const val PASSWORD = "secret"
                
                private var connectionCount = 0
                
                fun connect() {
                    connectionCount++
                    println("Подключение к БД. Всего подключений: ${'$'}connectionCount")
                }
                
                fun disconnect() {
                    connectionCount--
                    println("Отключение от БД. Осталось: ${'$'}connectionCount")
                }
            }
            
            // Использование
            DatabaseConfig.connect()
            DatabaseConfig.connect()
            DatabaseConfig.disconnect()
            ```

            ### Companion object (аналог статических членов)

            В Kotlin нет ключевого слова `static`. Вместо него используется `companion object`.

            ```kotlin
            class MyClass {
                companion object {
                    const val CONSTANT = "Значение"
                    private var instanceCount = 0
                    
                    fun create(): MyClass {
                        instanceCount++
                        println("Создан экземпляр #${'$'}instanceCount")
                        return MyClass()
                    }
                    
                    fun getInstanceCount(): Int = instanceCount
                }
                
                fun doSomething() {
                    println("Метод экземпляра, константа: ${'$'}CONSTANT")
                }
            }
            
            // Использование
            val obj = MyClass.create()
            println(MyClass.CONSTANT)
            println("Всего создано: ${'$'}{MyClass.getInstanceCount()}")
            ```

            ### Анонимные объекты (object expressions)

            Используются для создания объектов "на лету", часто для реализации интерфейсов.

            ```kotlin
            interface ClickListener {
                fun onClick()
                fun onLongClick()
            }
            
            class Button {
                var clickListener: ClickListener? = null
                
                fun simulateClick() {
                    clickListener?.onClick()
                }
            }
            
            fun main() {
                val button = Button()
                
                // Создание анонимного объекта
                button.clickListener = object : ClickListener {
                    override fun onClick() {
                        println("Кнопка нажата!")
                    }
                    
                    override fun onLongClick() {
                        println("Долгое нажатие!")
                    }
                }
                
                button.simulateClick()
            }
            ```

            ## 🧬 Вложенные и внутренние классы

            ```kotlin
            class Outer {
                private val secret = "Секрет"
                
                // Вложенный класс (не имеет доступа к членам Outer)
                class Nested {
                    fun foo() = "Nested"
                }
                
                // Внутренний класс (имеет доступ к членам Outer)
                inner class Inner {
                    fun getSecret() = "Секрет: ${'$'}secret"  // доступ к private члену
                }
            }
            
            fun main() {
                val nested = Outer.Nested()  // создаётся без экземпляра Outer
                val inner = Outer().Inner()   // нужен экземпляр Outer
            }
            ```

            ## ⚡ Свойства (Properties) и поля

            В Kotlin свойства заменяют поля и методы getter/setter из Java.

            ### Полный синтаксис свойства

            ```kotlin
            class Person {
                var name: String = ""
                    get() = field.uppercase()  // кастомный getter
                    set(value) {
                        field = value.trim()   // кастомный setter
                    }
                
                var age: Int = 0
                    set(value) {
                        if (value >= 0 && value < 150) {
                            field = value
                        } else {
                            println("Некорректный возраст")
                        }
                    }
                
                val isAdult: Boolean
                    get() = age >= 18  // вычисляемое свойство
                
                lateinit var address: String  // поздняя инициализация (для non-null)
            }
            ```

            ### Делегированные свойства

            ```kotlin
            import kotlin.properties.Delegates
            import kotlin.reflect.KProperty
            
            class Example {
                // Ленивая инициализация
                val lazyValue: String by lazy {
                    println("Вычисляется один раз!")
                    "Результат"
                }
                
                // Наблюдаемое свойство
                var observed: String by Delegates.observable("начальное") { 
                    prop, old, new ->
                    println("${'$'}{prop.name} изменилось с ${'$'}old на ${'$'}new")
                }
                
                // Свойство, которое нельзя установить в null после инициализации
                var notNull: String by Delegates.notNull<String>()
            }
            ```

            ## 📈 Sealed классы (запечатанные классы)

            Sealed классы позволяют создавать ограниченную иерархию классов. Все наследники известны на этапе компиляции.

            ```kotlin
            sealed class Result<out T> {
                data class Success<T>(val data: T) : Result<T>()
                data class Error(val message: String) : Result<Nothing>()
                object Loading : Result<Nothing>()
            }
            
            fun handleResult(result: Result<String>) {
                when (result) {
                    is Result.Success -> println("Успех: ${'$'}{result.data}")
                    is Result.Error -> println("Ошибка: ${'$'}{result.message}")
                    Result.Loading -> println("Загрузка...")
                }  // when должен быть исчерпывающим (не нужен else)
            }
            ```

            ## 🎯 Практические примеры

            ### Фабричный метод

            ```kotlin
            class Connection private constructor(val url: String) {
                companion object {
                    fun create(url: String): Connection? {
                        return if (url.isNotEmpty()) Connection(url) else null
                    }
                }
            }
            ```

            ### Строитель (Builder pattern)

            ```kotlin
            class Pizza private constructor(
                val size: String,
                val cheese: Boolean,
                val pepperoni: Boolean,
                val mushrooms: Boolean
            ) {
                class Builder {
                    private var size: String = "Medium"
                    private var cheese: Boolean = false
                    private var pepperoni: Boolean = false
                    private var mushrooms: Boolean = false
                    
                    fun setSize(size: String) = apply { this.size = size }
                    fun addCheese() = apply { cheese = true }
                    fun addPepperoni() = apply { pepperoni = true }
                    fun addMushrooms() = apply { mushrooms = true }
                    
                    fun build() = Pizza(size, cheese, pepperoni, mushrooms)
                }
            }
            
            // Использование
            val pizza = Pizza.Builder()
                .setSize("Large")
                .addCheese()
                .addPepperoni()
                .build()
            ```

            ## 📚 Резюме

            ✅ **Классы** — шаблоны для создания объектов  
            ✅ **Конструкторы** — первичные и вторичные  
            ✅ **Наследование** — open классы, override методов  
            ✅ **Интерфейсы** — множественное наследование поведения  
            ✅ **Data классы** — автоматическая генерация методов  
            ✅ **Enum классы** — ограниченный набор констант  
            ✅ **Object** — синглтоны, companion object, анонимные объекты  
            ✅ **Sealed классы** — ограниченная иерархия  
            ✅ **Свойства** — с кастомными getter/setter  

            Kotlin делает ООП более выразительным и безопасным, убирая много шаблонного кода, который был неизбежен в Java!
        """.trimIndent(),
        codeExample = """
            fun main() {
                println("🏭 Демонстрация ООП в Kotlin\n")
                
                // 1️⃣ Базовый класс
                println("1️⃣ Базовый класс")
                val person = Person("Алексей", 30)
                person.speak()
                person.haveBirthday()
                person.speak()
                println()
                
                // 2️⃣ Наследование
                println("2️⃣ Наследование")
                val dog = Dog("Бобик", "Овчарка")
                val cat = Cat("Мурка")
                
                dog.makeSound()
                dog.fetch()
                cat.makeSound()
                println()
                
                // 3️⃣ Абстрактные классы
                println("3️⃣ Абстрактные классы")
                val circle = Circle(5.0)
                val rectangle = Rectangle(4.0, 6.0)
                
                println("Круг: площадь = ${'$'}{circle.area()}, периметр = ${'$'}{circle.perimeter()}")
                println("Прямоугольник: площадь = ${'$'}{rectangle.area()}, периметр = ${'$'}{rectangle.perimeter()}")
                circle.description()
                rectangle.description()
                println()
                
                // 4️⃣ Интерфейсы
                println("4️⃣ Интерфейсы")
                val shape = DrawableRectangle(10, 20)
                shape.draw()
                shape.drawOutline()
                shape.move(5, 10)
                println("Скорость перемещения: ${'$'}{shape.speed}")
                println()
                
                // 5️⃣ Data классы
                println("5️⃣ Data классы")
                val user1 = User(1, "Иван", "ivan@example.com")
                val user2 = User(1, "Иван", "ivan@example.com")
                val user3 = user1.copy(name = "Петр", email = "petr@example.com")
                
                println("user1: ${'$'}user1")
                println("user2: ${'$'}user2")
                println("user1 == user2: ${'$'}{user1 == user2}")  // true
                println("user1 == user3: ${'$'}{user1 == user3}")  // false
                
                // Деструктуризация
                val (id, name, email) = user1
                println("Деструктуризация: id=${'$'}id, name=${'$'}name, email=${'$'}email")
                println()
                
                // 6️⃣ Enum классы
                println("6️⃣ Enum классы")
                val color = Color.RED
                println("Цвет: ${'$'}color")
                println("RGB: ${'$'}{color.rgb}")
                println("Описание: ${'$'}{color.description()}")
                println("Содержит красный? ${'$'}{color.containsRed()}")
                
                processStatus(Status.ACTIVE)
                processStatus(Status.BLOCKED)
                
                // Использование enum с анонимными классами
                val result = Operation.PLUS.apply(10, 5)
                println("10 + 5 = ${'$'}result")
                println("10 * 5 = ${'$'}{Operation.MULTIPLY.apply(10, 5)}")
                println()
                
                // 7️⃣ Объекты и синглтоны
                println("7️⃣ Объекты и синглтоны")
                DatabaseConfig.connect()
                DatabaseConfig.connect()
                DatabaseConfig.disconnect()
                DatabaseConfig.disconnect()
                println()
                
                // 8️⃣ Companion object
                println("8️⃣ Companion object")
                val obj1 = MyClass.create()
                val obj2 = MyClass.create()
                val obj3 = MyClass.create()
                println("Константа: ${'$'}{MyClass.CONSTANT}")
                println("Создано экземпляров: ${'$'}{MyClass.getInstanceCount()}")
                obj1.doSomething()
                println()
                
                // 9️⃣ Вложенные и внутренние классы
                println("9️⃣ Вложенные и внутренние классы")
                val nested = Outer.Nested()
                println("Вложенный класс: ${'$'}{nested.foo()}")
                
                val outer = Outer()
                val inner = outer.Inner()
                println("Внутренний класс: ${'$'}{inner.getSecret()}")
                println()
                
                // 🔟 Свойства и делегаты
                println("🔟 Свойства и делегаты")
                val example = PropertyExample()
                println("Ленивое свойство (первый доступ): ${'$'}{example.lazyValue}")
                println("Ленивое свойство (второй доступ): ${'$'}{example.lazyValue}")
                
                example.observed = "Первое значение"
                example.observed = "Второе значение"
                
                try {
                    // example.notNull // Ошибка! не инициализировано
                    example.notNull = "Теперь инициализировано"
                    println("notNull: ${'$'}{example.notNull}")
                } catch (e: Exception) {
                    println("Ошибка: ${'$'}{e.message}")
                }
                println()
                
                // 1️⃣1️⃣ Sealed классы
                println("1️⃣1️⃣ Sealed классы")
                val results = listOf(
                    Result.Success("Данные загружены"),
                    Result.Error("Ошибка сети"),
                    Result.Loading
                )
                
                results.forEach { result ->
                    handleResult(result)
                }
                println()
                
                // 1️⃣2️⃣ Builder pattern
                println("1️⃣2️⃣ Builder pattern")
                val pizza = Pizza.Builder()
                    .setSize("Large")
                    .addCheese()
                    .addPepperoni()
                    .addMushrooms()
                    .build()
                
                println("Пицца: ${'$'}pizza")
                println()
                
                // 1️⃣3️⃣ Анонимные объекты
                println("1️⃣3️⃣ Анонимные объекты")
                val button = Button()
                
                button.clickListener = object : ClickListener {
                    override fun onClick() {
                        println("Кнопка нажата!")
                    }
                    
                    override fun onLongClick() {
                        println("Долгое нажатие на кнопку!")
                    }
                    
                    override fun onDoubleClick() {
                        println("Двойной клик!")
                    }
                }
                
                button.simulateClick()
                button.simulateLongClick()
                button.simulateDoubleClick()
                println()
                
                // 1️⃣4️⃣ Фабричный метод
                println("1️⃣4️⃣ Фабричный метод")
                val connection = Connection.create("jdbc:mysql://localhost:3306/mydb")
                val badConnection = Connection.create("")
                
                println("Соединение создано? ${'$'}{connection != null}")
                println("Плохое соединение создано? ${'$'}{badConnection != null}")
            }
            
            // 1️⃣ Базовый класс
            open class Person(val name: String, var age: Int) {
                open fun speak() {
                    println("Привет, меня зовут ${'$'}name, мне ${'$'}age лет")
                }
                
                fun haveBirthday() {
                    age++
                    println("🎂 С днём рождения! Теперь мне ${'$'}age лет")
                }
            }
            
            // 2️⃣ Наследование
            open class Animal(val name: String) {
                open fun makeSound() {
                    println("Животное издает звук")
                }
            }
            
            class Dog(name: String, val breed: String) : Animal(name) {
                override fun makeSound() {
                    println("🐕 ${'$'}name (${'$'}breed) говорит: Гав-гав!")
                }
                
                fun fetch() {
                    println("🦴 ${'$'}name приносит палку")
                }
            }
            
            class Cat(name: String) : Animal(name) {
                override fun makeSound() {
                    println("🐈 ${'$'}name говорит: Мяу!")
                }
            }
            
            // 3️⃣ Абстрактные классы
            abstract class Shape {
                abstract fun area(): Double
                abstract fun perimeter(): Double
                
                fun description() {
                    println("📐 Я фигура с площадью ${'$'}{area()} и периметром ${'$'}{perimeter()}")
                }
            }
            
            class Circle(val radius: Double) : Shape() {
                override fun area(): Double = Math.PI * radius * radius
                override fun perimeter(): Double = 2 * Math.PI * radius
            }
            
            class Rectangle(val width: Double, val height: Double) : Shape() {
                override fun area(): Double = width * height
                override fun perimeter(): Double = 2 * (width + height)
            }
            
            // 4️⃣ Интерфейсы
            interface Drawable {
                fun draw()
                fun drawOutline() {
                    println("✏️ Рисую контур")
                }
            }
            
            interface Movable {
                fun move(dx: Int, dy: Int)
                val speed: Int
                    get() = 10
            }
            
            class DrawableRectangle(val width: Int, val height: Int) : Drawable, Movable {
                override fun draw() {
                    println("🟦 Рисую прямоугольник ${'$'}width x ${'$'}height")
                }
                
                override fun move(dx: Int, dy: Int) {
                  println("Перемещаю на (${'$'}dx, ${'$'}dy)")
                }
                
                override val speed: Int = 20
            }
            
            // 5️⃣ Data классы
            data class User(
                val id: Int,
                val name: String,
                val email: String
            )
            
            // 6️⃣ Enum классы
            enum class Status {
                ACTIVE, INACTIVE, PENDING, BLOCKED
            }
            
            fun processStatus(status: Status) {
                val message = when (status) {
                    Status.ACTIVE -> "✅ Пользователь активен"
                    Status.INACTIVE -> "⏸️ Пользователь неактивен"
                    Status.PENDING -> "⏳ Ожидает подтверждения"
                    Status.BLOCKED -> "🚫 Пользователь заблокирован"
                }
                println(message)
            }
            
            enum class Color(val rgb: Int) {
                RED(0xFF0000),
                GREEN(0x00FF00),
                BLUE(0x0000FF),
                YELLOW(0xFFFF00);
                
                fun containsRed(): Boolean = (rgb and 0xFF0000) != 0
                
                fun description(): String = when (this) {
                    RED -> "Красный"
                    GREEN -> "Зеленый"
                    BLUE -> "Синий"
                    YELLOW -> "Желтый"
                }
            }
            
            enum class Operation {
                PLUS {
                    override fun apply(x: Int, y: Int): Int = x + y
                },
                MINUS {
                    override fun apply(x: Int, y: Int): Int = x - y
                },
                MULTIPLY {
                    override fun apply(x: Int, y: Int): Int = x * y
                },
                DIVIDE {
                    override fun apply(x: Int, y: Int): Int = x / y
                };
                
                abstract fun apply(x: Int, y: Int): Int
            }
            
            // 7️⃣ Объекты и синглтоны
            object DatabaseConfig {
                const val URL = "jdbc:mysql://localhost:3306/mydb"
                const val USER = "root"
                
                private var connectionCount = 0
                
                fun connect() {
                    connectionCount++
                    println("🔌 Подключение к БД. Всего подключений: ${'$'}connectionCount")
                }
                
                fun disconnect() {
                    if (connectionCount > 0) {
                        connectionCount--
                        println("🔌 Отключение от БД. Осталось: ${'$'}connectionCount")
                    }
                }
            }
            
            // 8️⃣ Companion object
            class MyClass {
                companion object {
                    const val CONSTANT = "🚀 Значение"
                    private var instanceCount = 0
                    
                    fun create(): MyClass {
                        instanceCount++
                        return MyClass()
                    }
                    
                    fun getInstanceCount(): Int = instanceCount
                }
                
                fun doSomething() {
                    println("Метод экземпляра, константа: ${'$'}CONSTANT")
                }
            }
            
            // 9️⃣ Вложенные и внутренние классы
            class Outer {
                private val secret = "🤫 Секретные данные"
                
                class Nested {
                    fun foo() = "Вложенный класс"
                }
                
                inner class Inner {
                    fun getSecret() = "Внутренний класс: ${'$'}secret"
                }
            }
            
            // 🔟 Свойства и делегаты
            class PropertyExample {
                val lazyValue: String by lazy {
                    println("⚡ Вычисление ленивого значения...")
                    "Результат вычисления"
                }
                
                var observed: String by Delegates.observable("начальное") { prop, old, new ->
                    println("📊 ${'$'}{prop.name} изменилось: '${'$'}old' -> '${'$'}new'")
                }
                
                var notNull: String by Delegates.notNull<String>()
            }
            
            // 1️⃣1️⃣ Sealed классы
            sealed class Result<out T> {
                data class Success<T>(val data: T) : Result<T>()
                data class Error(val message: String) : Result<Nothing>()
                object Loading : Result<Nothing>()
            }
            
            fun handleResult(result: Result<String>) {
                when (result) {
                    is Result.Success -> println("✅ Успех: ${'$'}{result.data}")
                    is Result.Error -> println("❌ Ошибка: ${'$'}{result.message}")
                    Result.Loading -> println("⏳ Загрузка...")
                }
            }
            
            // 1️⃣2️⃣ Builder pattern
            class Pizza private constructor(
                val size: String,
                val cheese: Boolean,
                val pepperoni: Boolean,
                val mushrooms: Boolean
            ) {
                class Builder {
                    private var size: String = "Medium"
                    private var cheese: Boolean = false
                    private var pepperoni: Boolean = false
                    private var mushrooms: Boolean = false
                    
                    fun setSize(size: String) = apply { this.size = size }
                    fun addCheese() = apply { cheese = true }
                    fun addPepperoni() = apply { pepperoni = true }
                    fun addMushrooms() = apply { mushrooms = true }
                    
                    fun build() = Pizza(size, cheese, pepperoni, mushrooms)
                }
                
                override fun toString(): String {
                    val toppings = mutableListOf<String>()
                    if (cheese) toppings.add("🧀 сыр")
                    if (pepperoni) toppings.add("🍖 пепперони")
                    if (mushrooms) toppings.add("🍄 грибы")
                    
                    return if (toppings.isEmpty()) {
                        "🍕 Пицца ${'$'}size (без начинок)"
                    } else {
                        "🍕 Пицца ${'$'}size с${'$'}{toppings.joinToString("")}"
                    }
                }
            }
            
            // 1️⃣3️⃣ Анонимные объекты
            interface ClickListener {
                fun onClick()
                fun onLongClick()
                fun onDoubleClick()
            }
            
            class Button {
                var clickListener: ClickListener? = null
                
                fun simulateClick() {
                    println("👆 Имитация клика")
                    clickListener?.onClick()
                }
                
                fun simulateLongClick() {
                    println("👆👆 Имитация долгого нажатия")
                    clickListener?.onLongClick()
                }
                
                fun simulateDoubleClick() {
                    println("👆👆👆 Имитация двойного клика")
                    clickListener?.onDoubleClick()
                }
            }
            
            // 1️⃣4️⃣ Фабричный метод
            class Connection private constructor(val url: String) {
                companion object {
                    fun create(url: String): Connection? {
                        return if (url.isNotEmpty()) {
                            println("✅ Создано соединение с ${'$'}url")
                            Connection(url)
                        } else {
                            println("❌ URL не может быть пустым")
                            null
                        }
                    }
                }
            }
        """.trimIndent()
    )
}