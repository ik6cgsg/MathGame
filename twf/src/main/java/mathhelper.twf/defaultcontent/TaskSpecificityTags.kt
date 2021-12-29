package mathhelper.twf.defaultcontent


enum class TaskTagCode(val code: String,
                       val nameEn: String,
                       val nameRu: String,
                       val descriptionEn: String = nameEn,
                       val descriptionRu: String = nameRu) {
    PROOF("Proof", "Proof", "Доказательство"),
    SIMPLIFICATION("Simplification", "Simplification", "Упрощение"),
    COMPUTATION("Computation", "Computation", "Вычисление"),
    EQUATION("Equation", "Equation", "Уравнение"),

    FACTORIZATION("Factorization", "Factorization", "Разложение на множители"),
    REDUCE("Reduce", "Factorization", "Разложение на множители"),

    FORMULA_BASE("FormulaBase", "Formula Application Task", "Задача на применение формулы"),
    FORMULA_DEDUCE("FormulaDeduce", "Formula Deducing Task", "Задача, в которой выводится формула"),
    TRICK("Trick", "Task with Trick", "Задача с изюминкой"),


    // solution patterns
    SHORT_MULTIPLICATION("ShortMultiplication", "Short Multiplication", "Сокращенное умножение"),
    SUM_SQRS("SumSqrs", "Sum of Squares", "Сумма квадратов"),
    DIFF_SQRS("DiffSqrs", "Difference of Squares", "Разность квадратов"),
    SQR_SUM("SqrSum", "Square of Sum", "Квадрат суммы"),
    SQR_DIFF("SqrDiff", "Square of Difference", "Квадрат разности"),
    SUM_CUBES("SumCubes", "Sum of Cubes", "Сумма кубов"),
    DIFF_CUBES("DiffCubes", "Difference of Cubes", "Разность кубов"),
    CUBE_SUM("CubeSum", "Cube of Sum", "Куб суммы"),
    CUBE_DIFF("CubeDiff", "Cube of Difference", "Куб разности"),

    // scientific area
    FRACTION("Fraction", "Fraction", "Дробь"),

    TRIGONOMETRY("Trigonometry", "Trigonometry", "Тригонометрия"),
    INVERSE_TRIGONOMETRY("InverseTrigonometry", "Inverse Trigonometry Functions", "Обратные тригонометрические функции"),
    DEGREES("Degrees", "Degrees of Angles", "Градусные муры углов"),
    PYTHAGOREAN_IDENTITY("PythagoreanIdentity", "Pythagorean Trigonometric Identity", "Основное тригонометрическое тождество"),

    TRIGONOMETRY_ANGLE_SUM("TrigonometryAngleSum", "Expanding of Sum or Difference of Angle in Trigonometry Function", "Раскрытие тригонометрических функций от суммы и разности"),
    TRIGONOMETRY_SUM("TrigonometrySum", "Expanding of Sum or Difference of Trigonometry Functions", "Раскрытие суммы или разности тригонометрических функций"),
    TRIGONOMETRY_PRODUCT("TrigonometryProduct", "Expanding of Product of Trigonometry Functions", "Раскрытие произведения тригонометрических функций"),
    TRIGONOMETRY_REFLECTIONS("TrigonometryReflections", "Trigonometry Reflection Formulas", "Формулы приведения в тригонометрии"),

    LOGARITHM("Logarithm", "Logarithm", "Логарифм"),


    LOGIC("Logic", "Logic", "Логика"),

    NORMAL_FORMS("NormalForms", "Normal Forms", "Нормальные формы"),
    CNF("CNF", "CNF", "КНФ"),
    DNF("DNF", "DNF", "ДНФ"),

    RESOLUTION("Resolution", "Resolution", "Резолюции")
}

enum class TaskSetTagCode(val code: String,
                          val nameEn: String,
                          val nameRu: String,
                          val descriptionEn: String = nameEn,
                          val descriptionRu: String = nameRu) {
    TRIGONOMETRY("Trigonometry", "Trigonometry", "Тригонометрия"),
    LOGIC("Logic", "Logic", "Логика"),

    NORMAL_FORMS("NormalForms", "Normal Forms", "Нормальные формы"),
    RESOLUTION("Resolution", "Resolution", "Резолюции"),

    CHECK_YOURSELF("CheckYourself", "Check Yourself", "Проверь себя"),
    STEP_BY_STEP("StepByStep", "Step by Step", "Шаг за шагом"),
    TRAIN_SET("TrainSet", "Train Set", "Тренировка"),
    EXTRAORDINARY("Extraordinary", "Extraordinary", "Необычное")
}