package mathhelper.twf.defaultcontent.defaulttasksets

import mathhelper.twf.api.stringToStructureString
import mathhelper.twf.config.*
import mathhelper.twf.defaultcontent.TaskSetTagCode
import mathhelper.twf.defaultcontent.TaskTagCode.*
import mathhelper.twf.standartlibextensions.toCustomCodeSuffixPart

class TriginometryTaskSets {
    companion object {

        val trigonometryStepByStepTasksVladU = listOf<TaskITR>(
                //1. Definition of trigonometry functions
                //  - градусные и радианные меры углов
                //  - значения тригонометрических функций в основных углах
                //  - отношения между тригонометрическими функциями по определению (tg = sin / cos; ctg = cos / sin)
                //  - периоды функций (приведение с разницей до pi и 2*pi)

                //2. Основное тригонометрическое тождество
                //3. Формулы суммы и разности
                //  - Формулы двойного угла
                //4. Формулы приведения
                //  - sin к cos и обратно
                //  - sin и cos к tg и ctg и обратно
                TaskITR(
                        originalExpressionStructureString = "(cos(/(*(8;pi);3)))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 1.1,
                        tags = mutableSetOf(), // TODO: maybe new tags are needed
                        comment = "16 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(1;-(^(tg(/(x;2));2)));+(1;^(tg(/(x;2));2))))",
                        goalExpressionStructureString = "(cos(x))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 6.0,
                        tags = mutableSetOf(FRACTION.code, FORMULA_DEDUCE.code),
                        interestingFacts = mapOf("data" to InterestingFact("Cosine though tangent formula", "Формула разложения косинуса через тангенс", "beforeBeginning")),
                        comment = "5 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(cos(/(*(18;pi);5)))",
                        nameEn = "Positive acute angle",
                        nameRu = "Положительный острый угол",
                        descriptionShortEn = "Write using a positive acute angle",
                        descriptionShortRu = "Запишите через положительный острый угол",
                        descriptionEn = "Write using a positive acute angle",
                        descriptionRu = "Приведите тригонометрическую функцию произвольного аргумента к тригонометрической функции острого угла",
                        goalType = "oneOfHiddenGoals",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(cos(/(*(2;pi);5)))")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 1.6,
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        comment = "24 vlad_u"
                )

                //5. Преобразование суммы в произведение и обратно
                //6. Обратные тригонометрические функции
                //  -- определение
                //  -- задачи на выражение (уравнения)
        )


        val checkYourSelfSimpleTrigonometryTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(/(+(1;-(cos(x));cos(*(2;x)));+(sin(*(2;x));-(sin(x)))))",
                        goalExpressionStructureString = "(ctg(x))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.7,
                        tags = mutableSetOf(FRACTION.code, PYTHAGOREAN_IDENTITY.code, TRIGONOMETRY_ANGLE_SUM.code),
                        comment = "26 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(tg(/(π;4));-(sin(+(*(2;π);x))));cos(x)))",
                        goalExpressionStructureString = "(/(cos(x);+(1;sin(x))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.9,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRICK.code, FRACTION.code, DIFF_SQRS.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "10 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(2;cos(*(3;x));cos(x));-(cos(*(2;x)));-(cos(*(4;x)))))",
                        goalType = "simplification",
                        goalPattern = "?:0:?:?N",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.0,
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_PRODUCT.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        comment = "76 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(*(3;sin(+(x;-(*(9;π)))));cos(+(x;/(π;2))));sin(+(π;-(x)))))",
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.0,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_REFLECTIONS.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(4)))")
                        ),
                        comment = "75 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(/(*(cos(x);tg(x));^(sin(x);2));-(*(ctg(x);cos(x)))))",
                        goalExpressionStructureString = "(sin(x))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.5,
                        tags = mutableSetOf(FRACTION.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "30 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(sin(*(+(*(ctg(/(π;2));sin(x));1;tg(/(π;4)));x)))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(+(sin(x);cos(x)))", rightStructureString = "(/(5;4))", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        difficulty = 3.2,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRICK.code, SQR_SUM.code, PYTHAGOREAN_IDENTITY.code, TRIGONOMETRY_ANGLE_SUM.code),
                        comment = "39 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(*(3;sin(x));-(cos(x)));+(sin(x);*(2;cos(x)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(tg(x))", rightStructureString = "(5)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        difficulty = 2.6,
                        tags = mutableSetOf(TRICK.code, FRACTION.code),
                        comment = "43 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(tg(x);2);-(*(^(tg(x);2);^(sin(x);2)));-(^(sin(x);2))))",
                        goalType = "simplification",
                        goalPattern = "?:0:?:?N",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.7,
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, FRACTION.code),
                        comment = "49 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(sin(x);cos(*(6;x)));-(*(sin(*(3;x));cos(*(4;x))))))",
                        goalExpressionStructureString = "(+(-(*(cos(*(3;x));sin(*(2;x))))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.9,
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, TRIGONOMETRY_SUM.code),
                        comment = "77 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(*(cos(/(π;9));cos(/(*(2;π);9));cos(/(*(4;π);9))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.7,
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, TRIGONOMETRY_SUM.code, TRIGONOMETRY_REFLECTIONS.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.125)", "(/(1;8))")
                        ),
                        comment = "79 vlad_u"
                )
        )

        val checkYourSelfMiddleTrigonometryTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(+(tg(*(4;x));-(/(1;cos(*(4;x))))))",
                        goalExpressionStructureString = "(/(+(sin(*(2;x));-(cos(*(2;x))));+(sin(*(2;x));cos(*(2;x)))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.8,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, FRACTION.code, DIFF_SQRS.code, SQR_DIFF.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "6 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(+(*(sin(/(*(3;pi);10));cos(/(*(2;pi);5)));*(cos(/(*(3;pi);10));sin(/(*(2;pi);5))));2);^(+(*(cos(/(pi;10));cos(/(*(3;pi);5)));-(*(sin(/(pi;10));sin(/(*(3;pi);5)))));2)))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.4,
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, TRIGONOMETRY_ANGLE_SUM.code),
                        comment = "27 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(sin(*(4;x));*(cos(*(4;x));ctg(*(2;x)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(tg(*(2;x)))", rightStructureString = "(4)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        difficulty = 4.6,
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        comment = "13 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(1;cos(/(x;2));-(sin(/(x;2))));+(1;-(cos(/(x;2)));-(sin(/(x;2))))))",
                        goalExpressionStructureString = "(+(-(ctg(/(x;4)))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 5.8,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "33 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(sin(+(/(*(9;pi);8);x));2);-(^(sin(+(/(*(17;pi);8);-(x)));2))))",
                        goalExpressionStructureString = "(/(sin(*(2;x));^(2;0.5)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 6.2,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_SUM.code, DIFF_SQRS.code),
                        comment = "40 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(cos(x);cos(*(2;x));cos(*(3;x)));+(sin(x);sin(*(2;x));sin(*(3;x)))))",
                        goalExpressionStructureString = "(ctg(*(2;x)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 5.2,
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        comment = "48 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(cos(*(4;x));1);+(ctg(x);-(tg(x)))))",
                        goalExpressionStructureString = "(/(sin(*(4;x));2))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 5.0,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code, PYTHAGOREAN_IDENTITY.code, TRICK.code),
                        comment = "52 vlad_u solution: \\textcolor{purple}{\\frac{\\left(\\cos \\left(4\\cdot x\\right)+1\\right)}{\\left(\\operatorname{ctg}\\left(x\\right)-tg\\left(x\\right)\\right)}\\textcolor{green}{=}\\frac{\\left(\\cos \\left(4\\cdot x\\right)+1\\right)\\cdot \\left(\\sin \\left(x\\right)\\cdot \\cos \\left(x\\right)\\right)}{\\left(\\operatorname{ctg}\\left(x\\right)-tg\\left(x\\right)\\right)\\cdot \\left(\\sin \\left(x\\right)\\cdot \\cos \\left(x\\right)\\right)}\\textcolor{green}{=}\\frac{\\left(\\cos \\left(4\\cdot x\\right)+1\\right)\\cdot \\left(\\frac{\\sin \\left(2\\cdot x\\right)}{2}\\right)}{\\left(\\operatorname{ctg}\\left(x\\right)-tg\\left(x\\right)\\right)\\cdot \\left(\\sin \\left(x\\right)\\cdot \\cos \\left(x\\right)\\right)}\\textcolor{green}{=}\\frac{\\left(\\cos \\left(4\\cdot x\\right)+1\\right)\\cdot \\left(\\frac{\\sin \\left(2\\cdot x\\right)}{2}\\right)}{\\left(\\frac{\\cos \\left(x\\right)}{\\sin \\left(x\\right)}-tg\\left(x\\right)\\right)\\cdot \\left(\\sin \\left(x\\right)\\cdot \\cos \\left(x\\right)\\right)}\\textcolor{green}{=}\\frac{\\left(\\cos \\left(4\\cdot x\\right)+1\\right)\\cdot \\sin \\left(2\\cdot x\\right)}{2\\left(\\cos ^2\\left(x\\right)-\\sin ^2\\left(x\\right)\\right)}\\textcolor{red}{=}\\frac{2\\cos ^2\\left(2\\cdot x\\right)\\cdot \\sin \\left(2\\cdot x\\right)}{2\\cos \\left(2\\cdot x\\right)}=\\frac{\\sin \\left(4\\cdot x\\right)}{2}}"
                ),
                TaskITR(
                        originalExpressionStructureString = "(*(tg(/(π;9));tg(/(*(2;π);9));tg(/(*(3;π);9));tg(/(*(4;π);9))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 5.8,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_PRODUCT.code, TRIGONOMETRY_SUM.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(3)")
                        ),
                        comment = "78 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(sin(/(+(*(4;π);-(*(3;x)));3));sin(/(+(*(2;π);-(*(3;x)));3)));-(*(cos(+(/(π;3);x));cos(/(+(*(19;π);-(*(3;x)));3))))))",
                        goalExpressionStructureString = "(+(-(cos(*(2;x)))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.8,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_REFLECTIONS.code),
                        comment = "81 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(1;sin(x));+(1;-(sin(x)))))",
                        goalExpressionStructureString = "(^(tg(+(/(π;4);/(x;2)));2))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 5.1,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_REFLECTIONS.code),
                        comment = "83 vlad_u"
                )
// TODO: add product of sin / cos + task with beautiful result of computation
        )

        val checkYourSelfCompleteTrigonometryTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(/(+(*(2;^(sin(*(4;x));2));-(1));*(2;ctg(+(/(pi;4);*(4;x)));^(cos(+(/(*(5;pi);4);-(*(4;x))));2))))",
                        goalType = "simplification",
                        goalPattern = "+:0-1(-:1):?:?Z", // -1
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 6.4,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code, PYTHAGOREAN_IDENTITY.code, TRIGONOMETRY_REFLECTIONS.code),  // TODO: unknown solution
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(1)))")
                        ),
                        comment = "35 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(/(1;+(tg(*(3;x));tg(x)));-(/(1;+(ctg(*(3;x));ctg(x))))))",
                        goalExpressionStructureString = "(ctg(*(4;x)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 6.1,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code), // TODO: unknown solution
                        comment = "38 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(3;-(*(4;cos(*(2;x))));cos(*(4;x)));+(3;*(4;cos(*(2;x)));cos(*(4;x)))))",
                        goalExpressionStructureString = "(^(tg(x);4))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 5.4,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code), // TODO: unknown solution
                        comment = "41 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(sin(*(3;x));-(sin(x));sin(*(5;x));-(sin(*(7;x))));+(cos(x);-(cos(*(3;x)));cos(*(5;x));-(cos(*(7;x))))))",
                        goalType = "simplification",
                        goalPattern = "tg,* : (* : 2)",
                        difficulty = 6.5,
                        tags = mutableSetOf(FRACTION.code, TRIGONOMETRY_SUM.code), // TODO: unknown solution
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(*(2;x)))")
                        ),
                        comment = "34 vlad_u"
                )
        )

        val shortMultiplicationTrigonometryTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(^(+(sin(x);-(cos(x)));2))",
                        goalExpressionStructureString = "(+(1;-(sin(*(2;x)))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 1.5,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRIGONOMETRY_ANGLE_SUM.code, SQR_DIFF.code, PYTHAGOREAN_IDENTITY.code)
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg(x))^2 + (ctg(x))^2"),
                        goalExpressionStructureString = stringToStructureString("(tg(x) + ctg(x))^2 - 2"),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.5,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SQR_SUM.code, TRICK.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(*(+(2;*(2;sin(x)));+(1;-(sin(x))));*(+(1;cos(x));+(2;-(*(2;cos(x)))))))",
                        goalPattern = "?:0:?:?N",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(ctg(x))", rightStructureString = "(2)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        difficulty = 4.2,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, FRACTION.code, DIFF_SQRS.code),
                        comment = "1 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(1;-(^(sin(x);4)));^(cos(x);2)))",
                        goalExpressionStructureString = "(+(2;-(^(cos(x);2))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.2,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, FRACTION.code, DIFF_SQRS.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "8 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x))^4 + 2*cos(x)*(sin(x))^2 + (cos(x))^2"),
                        goalExpressionStructureString = stringToStructureString("(sin(x) + ctg(x))^2 * (sin(x))^2"),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.5,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SQR_SUM.code, TRICK.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(cos(*(4;x));*(4;cos(*(2;x)));3))",
                        goalExpressionStructureString = "(*(8;^(cos(x);4)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.2,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SQR_SUM.code, TRIGONOMETRY_ANGLE_SUM.code),
                        comment = "12 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(/(sin(x);+(1;cos(x)));/(+(1;cos(x));sin(x))))",
                        goalExpressionStructureString = "(/(2;sin(x)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.3,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, FRACTION.code, SQR_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "9 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(^(cos(x);3);-(^(sin(x);3)));+(cos(x);-(sin(x)))))",
                        goalExpressionStructureString = "(+(1;*(sin(x);cos(x))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.4,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, FRACTION.code, CUBE_DIFF.code),
                        comment = "3 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(^(sin(x);3);^(cos(x);3));+(sin(x);cos(x))))",
                        goalExpressionStructureString = "(+(1;-(/(sin(*(2;x));2))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 4.5,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, FRACTION.code, CUBE_SUM.code),
                        comment = "4 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(*(+(tg(x);ctg(x));+(/(4;^(sin(*(2;x));2));-(3))))",
                        goalExpressionStructureString = "(+(^(tg(x);3);^(ctg(x);3)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 5.5,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SUM_CUBES.code, TRICK.code, PYTHAGOREAN_IDENTITY.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(+(sin(x);-(cos(x)));2);sin(*(2;x))))",
                        goalType = "simplification",
                        goalPattern = "+:0-1(-:1):?:?R",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 4.3,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRIGONOMETRY_ANGLE_SUM.code, SQR_DIFF.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "28 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(*(+(1;-(cos(x)));+(1;cos(x)));sin(x)))",
                        goalExpressionStructureString = "(sin(x))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 4.0,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, DIFF_SQRS.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "29 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(cos(*(2;x));+(cos(x);-(sin(x)))))",
                        goalExpressionStructureString = "(+(sin(x);cos(x)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 3.3,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRIGONOMETRY_ANGLE_SUM.code, DIFF_SQRS.code),
                        comment = "31 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(1;-(sin(*(2;x))));+(cos(x);-(sin(x)))))",
                        goalExpressionStructureString = "(+(cos(x);-(sin(x))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 3.4,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRIGONOMETRY_ANGLE_SUM.code, SQR_DIFF.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "32 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(8;^(cos(x);6));*(8;^(sin(x);6));-(*(3;cos(*(4;x))))))",
                        goalType = "simplification",
                        goalPattern = "+:0-1(-:1):?:?R",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 5.3,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, TRIGONOMETRY_ANGLE_SUM.code, SUM_CUBES.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "36 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(1;*(2;sin(x);cos(x)));+(^(sin(x);2);-(^(cos(x);2)))))",
                        goalExpressionStructureString = "(/(+(tg(x);1);+(tg(x);-(1))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 4.6,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, PYTHAGOREAN_IDENTITY.code, DIFF_SQRS.code, SQR_SUM.code),
                        comment = "47 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(cos(x);2);-(^(sin(y);2))))",
                        goalExpressionStructureString = "(*(cos(+(x;y));cos(+(x;-(y)))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 3.7,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, DIFF_SQRS.code, TRIGONOMETRY_PRODUCT.code, TRIGONOMETRY_SUM.code),
                        comment = "84 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(sin(x);4);^(cos(x);4)))",
                        goalExpressionStructureString = "(/(+(3;cos(*(4;x)));4))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 5.7,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, DIFF_SQRS.code, TRICK.code, TRIGONOMETRY_ANGLE_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "93 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(sin(x);6);^(cos(x);6)))",
                        goalExpressionStructureString = "(/(+(5;*(3;cos(*(4;x))));8))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 6.4,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, DIFF_SQRS.code, SUM_SQRS.code, TRICK.code, TRIGONOMETRY_ANGLE_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "94 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(cos(x);4);-(^(sin(x);4))))",
                        goalExpressionStructureString = "(cos(*(2;x)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 2.3,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, DIFF_SQRS.code, TRIGONOMETRY_ANGLE_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "101 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(sin(x);6);^(cos(x);6);*(3;^(sin(x);2);^(cos(x);2))))",
                        goalType = "simplification",
                        goalPattern = "?:0:?:?N",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 3.4,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SUM_CUBES.code, SQR_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        comment = "102 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(^(sin(x);4);^(cos(x);4);-(1));+(^(sin(x);6);^(cos(x);6);-(1))))",
                        goalType = "simplification", // TODO: can I leave it without goalPattern?
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 6.7,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SUM_CUBES.code, SQR_SUM.code, TRICK.code, PYTHAGOREAN_IDENTITY.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(/(2;3))")
                        ),
                        comment = "104 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(tg(x);4);^(ctg(x);4)))",
                        descriptionEn = "Write using m = tg(x) + ctg(x)",
                        descriptionRu = "Запишите через m = tg(x) + ctg(x)",
                        goalType = "express",
                        goalPattern = "?:?:?:m",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 7.3,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SQR_SUM.code, TRICK.code), // TODO: something else?
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(^(m;4);-(*(4;^(m;2)));2))")
                        ),
                        rules = listOf(
                                RuleITR(leftStructureString = "(+(tg(x);ctg(x)))", rightStructureString = "(m)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL"),
                                RuleITR(leftStructureString = "(m)", rightStructureString = "(+(tg(x);ctg(x)))", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        comment = "103 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(*(4;cos(x));+(^(ctg(/(x;2));2);-(^(tg(/(x;2));2)))))",
                        goalExpressionStructureString = "(^(sin(x);2))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")), difficulty = 3.5,
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, DIFF_SQRS.code, TRIGONOMETRY_ANGLE_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "59 vlad_u"
                )
        )

        val TrigonometryTasksWithDegrees = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(sin(o(405)))",
                        nameEn = "Positive acute angle",
                        nameRu = "Положительный острый угол",
                        descriptionShortEn = "Write using a positive acute angle",
                        descriptionShortRu = "Запишите через положительный острый угол",
                        descriptionEn = "Write using a positive acute angle",
                        descriptionRu = "Приведите тригонометрическую функцию произвольного аргумента к тригонометрической функции острого угла",
                        goalType = "oneOfHiddenGoals",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(sin(o(45)))")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")), // TODO: rule pack does not have enough rules
                        difficulty = 1.5,
                        tags = mutableSetOf(DEGREES.code, TRIGONOMETRY_REFLECTIONS.code),
                        comment = "23 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(tg(o(863)))",
                        nameEn = "Positive acute angle",
                        nameRu = "Положительный острый угол",
                        descriptionShortEn = "Write using a positive acute angle",
                        descriptionShortRu = "Запишите через положительный острый угол",
                        descriptionEn = "Write using a positive acute angle",
                        descriptionRu = "Приведите тригонометрическую функцию произвольного аргумента к тригонометрической функции острого угла",
                        goalType = "oneOfHiddenGoals",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(tg(o(37)))))")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")), // TODO: rule pack does not have enough rules
                        difficulty = 1.7,
                        tags = mutableSetOf(DEGREES.code, TRIGONOMETRY_REFLECTIONS.code),
                        comment = "25 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(^(sin(+(-(o(585))));2))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 1.5,
                        tags = mutableSetOf(DEGREES.code, TRIGONOMETRY_REFLECTIONS.code),
                        comment = "15 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(*(sin(o(75));sin(o(15))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.4,
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, DEGREES.code, TRIGONOMETRY_REFLECTIONS.code),
                        comment = "11 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(cos(o(24));cos(o(5));cos(o(175));cos(o(204));cos(o(300))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.5,
                        tags = mutableSetOf(DEGREES.code, TRIGONOMETRY_REFLECTIONS.code),
                        comment = "14 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(/(1;*(2;sin(o(10))));-(*(2;sin(o(70))))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.9,
                        tags = mutableSetOf(DEGREES.code),
                        comment = "42 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(sin(o(15));4);^(cos(o(15));4)))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.1,
                        tags = mutableSetOf(DEGREES.code),
                        comment = "50 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(tg(o(15));2);^(ctg(o(15));2)))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.0,
                        tags = mutableSetOf(DEGREES.code),
                        comment = "51 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+(tg(o(210));ctg(o(210));tg(o(220));ctg(o(220)));+(sin(o(100));sin(o(40)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 7.4,
                        tags = mutableSetOf(DEGREES.code, FRACTION.code, TRIGONOMETRY_SUM.code, TRIGONOMETRY_ANGLE_SUM.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(/(8;3))")
                        ),
                        comment = "85 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(ctg(o(70));*(4;cos(o(70)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 7.1,
                        tags = mutableSetOf(DEGREES.code, FRACTION.code, TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_SUM.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(3;0.5))", "(^(3;/(1;2)))")
                        ),
                        comment = "87 vlad_u"
                )
        )

        val TrigonometryOtherTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(+(^(ctg(x);2);-(^(ctg(y);2))))",
                        goalExpressionStructureString = "(/(+(^(cos(x);2);-(^(cos(y);2)));*(^(sin(x);2);^(sin(y);2))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 3.5,
                        tags = mutableSetOf(FRACTION.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "91 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(^(+(cos(x);-(cos(y)));2);^(+(sin(x);-(sin(y)));2)))",
                        goalExpressionStructureString = "(*(4;^(sin(/(+(x;-(y));2));2)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.4,
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, PYTHAGOREAN_IDENTITY.code),
                        comment = "92 vlad_u"
                ),
                TaskITR(
                        originalExpressionStructureString = "(*(+(sin(x);-(cos(x)));+(sin(y);-(cos(y)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        difficulty = 2.4,
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(0.5)))", "(+(-(/(1;2))))")
                        ),
                        rules = listOf(
                                RuleITR(leftStructureString = "(sin(+(x;y)))", rightStructureString = "(0.8)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL"),
                                RuleITR(leftStructureString = "(cos(+(x;-(y))))", rightStructureString = "(0.3)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        comment = "95 vlad_u"
                )
        )

        val allTrigonometryTasks = shortMultiplicationTrigonometryTasks +
                checkYourSelfCompleteTrigonometryTasks +
                checkYourSelfSimpleTrigonometryTasks +
                checkYourSelfMiddleTrigonometryTasks +
                TrigonometryStepByStep.trigonometryStepByStepTasks


        fun createTrigonometryStepByStepTaskSet(
                nameSuffixEn: String,
                nameSuffixRu: String,
                descriptionShortEn: String? = null,
                descriptionShortRu: String? = null,
                topIndex: Int, subIndex: Int? = null,
                topNameEn: String? = null,
                topNameRu: String? = null,
                isCheckYourself: Boolean = false,
                tasks: List<TaskITR>
        ): TaskSetITR {
            val moduleNumberString = "${topIndex}.${subIndex?.toString() ?: ""}"
            val moduleNumberCodePart = "_${topIndex}_${if (subIndex != null) "${subIndex}_" else ""}${if (isCheckYourself) "z_" else ""}"
            val checkYourselfEn = if (isCheckYourself) "[Check Yourself]" else ""
            val checkYourselfRu = if (isCheckYourself) "[Проверь себя]" else ""
            val descriptionShortEnResult = descriptionShortEn
                    ?: "Expression transformations on ${nameSuffixEn.toLowerCase()}"
            val descriptionShortRuResult = descriptionShortRu
                    ?: "Преобразования выражений на ${nameSuffixRu.toLowerCase()}"
            val (topDescriptionPartEn, topDescriptionPartRu) = if (topNameEn != null && topNameRu != null && subIndex != null) Pair("$topNameEn $moduleNumberString", "$topNameRu $moduleNumberString") else Pair("", "")
            return TaskSetITR(
                    code = "TrigonometryStepByStep$moduleNumberCodePart${if (isCheckYourself) "CheckYourself" else ""}${nameSuffixEn.toCustomCodeSuffixPart()}",
                    nameEn = "[Trigonometry Step By Step]$checkYourselfEn ${moduleNumberString} $nameSuffixEn",
                    nameRu = "[Тригонометрия шаг за шагом]$checkYourselfRu ${moduleNumberString} $nameSuffixRu",
                    descriptionShortEn = descriptionShortEnResult,
                    descriptionShortRu = descriptionShortRuResult,
                    descriptionEn = "[Trigonometry Step By Step]$checkYourselfEn ${topIndex}. $topDescriptionPartEn $descriptionShortEnResult",
                    descriptionRu = "[Тригонометрия шаг за шагом]$checkYourselfRu ${topIndex}. $topDescriptionPartRu $descriptionShortRuResult",
                    subjectType = "standard_math",
                    tags = (setOf(TaskSetTagCode.STEP_BY_STEP.code, TaskSetTagCode.TRIGONOMETRY.code) + if (isCheckYourself) setOf(TaskSetTagCode.CHECK_YOURSELF.code) else setOf()).toMutableSet(),
                    tasks = tasks.map { it.copy() }
            )
        }

        val defaultTrigonometryTaskSets = listOf(
//        degreesAndRadians +
//                definitionOfSinCos +
//                definitionOfTgCtg +
//                trigonometricShiftsAndPeriodicity +
//                definitionOfTrigonometryFunctionsCheckYourself

                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Degrees and Radians", nameSuffixRu = "Градусы и радианы",
                        topIndex = 1, subIndex = 1,
                        topNameEn = "Function Definitions", topNameRu = "Определения функций",
                        tasks = TrigonometryStepByStep.degreesAndRadians
                ),
                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Sine and Cosine", nameSuffixRu = "Синус и косинус",
                        topIndex = 1, subIndex = 2,
                        topNameEn = "Function Definitions", topNameRu = "Определения функций",
                        tasks = TrigonometryStepByStep.definitionOfSinCos
                ),
                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Tangent and Cotangent", nameSuffixRu = "Тангенс и котангенс",
                        topIndex = 1, subIndex = 3,
                        topNameEn = "Function Definitions", topNameRu = "Определения функций",
                        tasks = TrigonometryStepByStep.definitionOfTgCtg
                ),
                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Shifts and Periodicity", nameSuffixRu = "Сдвиги и периодичность",
                        topIndex = 1, subIndex = 4,
                        topNameEn = "Function Definitions", topNameRu = "Определения функций",
                        tasks = TrigonometryStepByStep.trigonometricShiftsAndPeriodicity
                ),
                createTrigonometryStepByStepTaskSet(
                        isCheckYourself = true, nameSuffixEn = "Function Definitions", nameSuffixRu = "Определения функций",
                        topIndex = 1,
                        tasks = TrigonometryStepByStep.definitionOfTrigonometryFunctionsCheckYourself
                ),

                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Pythagorean Identity", nameSuffixRu = "Основное тригонометрическое тождество",
                        topIndex = 2,
                        tasks = TrigonometryStepByStep.pythagoreanIdentity
                ),
                createTrigonometryStepByStepTaskSet(
                        isCheckYourself = true, nameSuffixEn = "Pythagorean Identity", nameSuffixRu = "Основное тригонометрическое тождество",
                        topIndex = 2,
                        tasks = TrigonometryStepByStep.pythagoreanIdentityCheckYourself
                ),

                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Reflection formulas", nameSuffixRu = "Формулы приведения",
                        topIndex = 3,
                        tasks = TrigonometryStepByStep.trigonometryReflections
                ),
                createTrigonometryStepByStepTaskSet(
                        isCheckYourself = true, nameSuffixEn = "Reflection formulas", nameSuffixRu = "Формулы приведения",
                        topIndex = 3,
                        tasks = TrigonometryStepByStep.trigonometryReflectionsCheckYourself
                ),

                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Sine and Cosine of Angle Sum and Difference", nameSuffixRu = "Синус и косинус суммы и разности углов",
                        topIndex = 4, subIndex = 1,
                        topNameEn = "Sine and Cosine of Angle Sum and Difference", topNameRu = "Синус и косинус суммы и разности углов",
                        tasks = TrigonometryStepByStep.trigonometrySinCosAngleSumDiff
                ),
                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Double Angle", nameSuffixRu = "Двойной угол",
                        topIndex = 4, subIndex = 2,
                        topNameEn = "Sine and Cosine of Angle Sum and Difference", topNameRu = "Синус и косинус суммы и разности углов",
                        tasks = TrigonometryStepByStep.trigonometrySinCosAngleSumDiffWithDoubleArgs
                ),
                createTrigonometryStepByStepTaskSet(
                        isCheckYourself = true, nameSuffixEn = "Sine and Cosine of Angle Sum and Difference", nameSuffixRu = "Синус и косинус суммы и разности углов",
                        topIndex = 4,
                        tasks = TrigonometryStepByStep.trigonometrySinCosAngleSumDiffWithDoubleArgsCheckYourself
                ),

                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Sum and Product of Sine and Cosine", nameSuffixRu = "Сложение и умножение синуса и косинуса",
                        topIndex = 5,
                        tasks = TrigonometryStepByStep.trigonometrySumProdSinCos
                ),
                createTrigonometryStepByStepTaskSet(
                        isCheckYourself = true, nameSuffixEn = "Sum and Product of Sine and Cosine", nameSuffixRu = "Сложение и умножение синуса и косинуса",
                        topIndex = 5,
                        tasks = TrigonometryStepByStep.trigonometrySumProdSinCosCheckYourself
                ),

                createTrigonometryStepByStepTaskSet(
                        nameSuffixEn = "Advanced Operations with Tangent and Cotangent", nameSuffixRu = "Сложные операции над тангенсом и котангенсом",
                        topIndex = 6,
                        tasks = TrigonometryStepByStep.tgCtgSinCosCompositeTasks
                ),
                createTrigonometryStepByStepTaskSet(
                        isCheckYourself = true, nameSuffixEn = "Advanced Operations with Tangent and Cotangent", nameSuffixRu = "Сложные операции над тангенсом и котангенсом",
                        topIndex = 6,
                        tasks = TrigonometryStepByStep.tgCtgSinCosCompositeTasksCheckYourself
                ),
//        TaskSetITR(
//                code = "TrigonometryStepByStepDegreesAndRadians",
//                nameEn = "[Trigonometry Step By Step] 1.1 Degrees and Radians", nameRu = "[Тригонометрия шаг за шагом] 1.1 Градусы и радианы",
//                descriptionShortEn = "Expression transformations on degrees and radians",
//                descriptionShortRu = "Преобразования выражений на градусы и радианы",
//                descriptionEn = "[Trigonometry Step By Step] 1. Function Definitions 1.1 Expression transformations on degrees and radians",
//                descriptionRu = "[Тригонометрия шаг за шагом] 1. Определения функций 1.1 Преобразования выражений на градусы и радианы",
//                subjectType = "standard_math",
//                tasks = degreesAndRadians.map { it.copy() }
//        ),
//        TaskSetITR(
//                code = "TrigonometryStepByStepCheckYourselfFunctionDefinitions",
//                nameEn = "[Trigonometry Step By Step][Check Yourself] 1. Function Definitions", nameRu = "[Тригонометрия шаг за шагом][Проверь себя] 1. Определения функций",
//                subjectType = "standard_math",
//                tasks = definitionOfTrigonometryFunctionsCheckYourself.map { it.copy() }
//        ),

                TaskSetITR(
                        code = "CheckYourselfSimpleTrigonometry",
                        nameEn = "[Check Yourself] Simple Trigonometry", nameRu = "[Проверь себя] Простая тригонометрия",
                        descriptionShortEn = "Simple expression transformations in trigonometry",
                        descriptionShortRu = "Простые преобразования выражений в тригонометрии",
                        descriptionEn = "[Check Yourself] Complicated expression transformations in trigonometry",
                        descriptionRu = "[Проверь себя] Сложные преобразования выражений в тригонометрии",
                        subjectType = "standard_math",
                        tags = mutableSetOf(TaskSetTagCode.CHECK_YOURSELF.code, TaskSetTagCode.TRIGONOMETRY.code),
                        tasks = checkYourSelfSimpleTrigonometryTasks.map { it.copy() }
                ),
                TaskSetITR(
                        code = "CheckYourselfTrigonometry",
                        nameEn = "[Check Yourself] Trigonometry", nameRu = "[Проверь себя] Тригонометрия",
                        descriptionShortEn = "Expression transformations in trigonometry",
                        descriptionShortRu = "Преобразования выражений в тригонометрии",
                        descriptionEn = "[Check Yourself] Expression transformations in trigonometry",
                        descriptionRu = "[Проверь себя] Преобразования выражений в тригонометрии",
                        subjectType = "standard_math",
                        tags = mutableSetOf(TaskSetTagCode.CHECK_YOURSELF.code, TaskSetTagCode.TRIGONOMETRY.code),
                        tasks = checkYourSelfMiddleTrigonometryTasks.map { it.copy() }
                ),
                TaskSetITR(
                        code = "CheckYourselfCompleteTrigonometry",
                        nameEn = "[Check Yourself] Complete Trigonometry", nameRu = "[Проверь себя] Сложная тригонометрия",
                        descriptionShortEn = "Complicated expression transformations in trigonometry",
                        descriptionShortRu = "Сложные преобразования выражений в тригонометрии",
                        descriptionEn = "[Check Yourself] Complicated expression transformations in trigonometry",
                        descriptionRu = "[Проверь себя] Сложные преобразования выражений в тригонометрии",
                        subjectType = "standard_math",
                        tags = mutableSetOf(TaskSetTagCode.CHECK_YOURSELF.code, TaskSetTagCode.TRIGONOMETRY.code),
                        tasks = checkYourSelfCompleteTrigonometryTasks.map { it.copy() }
                ),
                TaskSetITR(
                        code = "TrigonometryShortMultiplication",
                        nameEn = "Short Multiplication in Trigonometry", nameRu = "Сокращенное умножение в тригонометрии",
                        descriptionShortEn = "New approach to formulas for short multiplication",
                        descriptionShortRu = "Выводи формулы сокращенного умножения по-новому",
                        descriptionEn = "Derive and use short multiplication formulas using trigonometric transformations",
                        descriptionRu = "Выводи и используй формулы сокращенного умножения с использованием тригонометрических преобразований",
                        subjectType = "standard_math",
                        tags = mutableSetOf(TaskSetTagCode.EXTRAORDINARY.code, TaskSetTagCode.TRIGONOMETRY.code),
                        tasks = shortMultiplicationTrigonometryTasks.map { it.copy() }
                )
        )
    }
}