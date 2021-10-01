package mathhelper.twf.defaultcontent.defaulttasksets

import mathhelper.twf.api.stringToExpression
import mathhelper.twf.api.stringToStructureString
import mathhelper.twf.config.*
import mathhelper.twf.defaultcontent.TaskTagCode.*

class TrigonometryStepByStep {
    companion object {

        fun basicTrigonometryFormulaComputationTask(originalExpressionStructureString: String, goalPattern: String = "+:0-1(-:1):?:?R") = TaskITR(
                originalExpressionStructureString = originalExpressionStructureString,
                goalType = "computation",
                goalPattern = goalPattern,
                rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                tags = mutableSetOf(FORMULA_BASE.code),
                difficulty = 0.1,
                otherCheckSolutionData = defaultOtherCheckSolutionData
        )

        fun basicTrigonometryFormulaSimplificationTask(originalExpressionStructureString: String, otherGoalData: Map<String, Any>, nameEn: String, nameRu: String) = TaskITR(
                descriptionShortEn = "Simplify by formula",
                descriptionShortRu = "Упростить по формуле",
                descriptionEn = "Simplify by formula",
                descriptionRu = "Упростить по формуле",
                originalExpressionStructureString = originalExpressionStructureString,
                goalType = "simplification",
                otherGoalData = otherGoalData,
                rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                tags = mutableSetOf(FORMULA_BASE.code),
                difficulty = 0.1
        )

        fun trigonometrySinCosSumReductionFormulaSimplificationTask(originalExpressionStructureString: String, otherGoalData: Map<String, Any>, nameEn: String, nameRu: String) = TaskITR(
                nameEn = nameEn,
                nameRu = nameRu,
                descriptionShortEn = "Expand by formula",
                descriptionShortRu = "Раскрыть по формуле",
                descriptionEn = "Expand by formula '$nameEn'",
                descriptionRu = "Раскрыть по формуле '$nameRu'",
                originalExpressionStructureString = originalExpressionStructureString,
                goalType = "simplification",
                otherGoalData = otherGoalData,
                rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                tags = mutableSetOf(FORMULA_BASE.code),
                difficulty = 0.1
        )

        fun trigonometrySumProdSinCosFormulaSimplificationTask(originalExpressionStructureString: String, otherGoalData: Map<String, Any>, nameEn: String, nameRu: String) = TaskITR(
                nameEn = nameEn,
                nameRu = nameRu,
                descriptionShortEn = "Expand by formula",
                descriptionShortRu = "Раскрыть по формуле",
                descriptionEn = "Expand by formula '$nameEn'",
                descriptionRu = "Раскрыть по формуле '$nameRu'",
                originalExpressionStructureString = originalExpressionStructureString,
                goalType = "simplification",
                otherGoalData = otherGoalData,
                rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                tags = mutableSetOf(FORMULA_BASE.code),
                difficulty = 0.1
        )

        val degreesAndRadians = listOf<TaskITR>(
                //TODO: support degrees rules and add tasks
        )

        val definitionOfSinCos = listOf(
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(sin(0))"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(sin(/(pi;6)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(sin(/(pi;4)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(sin(/(pi;3)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(sin(/(pi;2)))"),

                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(cos(0))"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(cos(/(pi;6)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(cos(/(pi;4)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(cos(/(pi;3)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(cos(/(pi;2)))"),

                TaskITR(
                        originalExpressionStructureString = "(+(^(sin(/(pi;4));2);-(cos(/(pi;3)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.5
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(2;sin(/(pi;2)));*(4;cos(pi))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(2)))")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.5
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(sin(/(pi;3));cos(/(pi;6));cos(/(pi;2)));*(8;sin(/(pi;6)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(4)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(pi/3)*cos(pi/6) + sin(pi))*(sin(pi/4)*cos(pi/4) - sin(pi/6))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("3*(sin(pi/6)/cos(pi/6))^2 - (cos(pi/3))^2 - (cos(pi/6))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),

                TaskITR(
                        originalExpressionStructureString = "(+(*(+(sin(x);-(cos(x)));/(cos(/(π;2));sin(/(π;2))));/(cos(/(π;4));sin(/(π;4)));1))",
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(+(/(^(3;sin(/(π;6)));2);-(/(/(cos(/(π;4));sin(/(π;4)));2)));sin(x));-(*(+(+(-(0.5));/(^(3;sin(/(π;6)));2));sin(x)));-(*(+(+(-(0.5));sin(/(π;6)));^(sin(x);3)))))",
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(+(1;-(/(cos(/(π;4));sin(/(π;4)))));+(sin(x);cos(x)));/(cos(/(π;4));sin(/(π;4)));/(sin(/(π;4));cos(/(π;4)))))",
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(pi/3)*cos(pi/6)+sin(pi/6)*cos(pi/3)-1) * (sin(pi/3)*sin(pi/4)*sin(pi/6) - cos(pi/3)*cos(pi/4)*cos(pi/6) + 0.5)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(*(+(/(^(3;cos(/(π;3)));2);-(sin(/(π;6))));sin(x));-(*(+(/(^(3;sin(/(π;6)));2);-(/(/(cos(/(π;4));sin(/(π;4)));2)));sin(x)));-(*(+(0.5;-(/(/(cos(/(π;4));sin(/(π;4)));2)));sin(x)))))",
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.9
                )
        )

        val definitionOfTgCtg = listOf(
                TaskITR(
                        originalExpressionStructureString = "(tg(x))",
                        nameEn = "Tangent",
                        nameRu = "Тангенс",
                        descriptionShortEn = "Express though sin and cos",
                        descriptionShortRu = "Выразить через sin и cos",
                        descriptionEn = "Express tangent though sine and cosine by formula",
                        descriptionRu = "Выразить тангенс через синус и косинус по формуле",
                        goalType = "simplification",
                        goalPattern = "!tg,!ctg : ? : ?",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        tags = mutableSetOf(FORMULA_BASE.code),
                        difficulty = 0.1
                ),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(tg(0))"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(tg(/(pi;6)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(tg(/(pi;4)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(tg(/(pi;3)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),

                TaskITR(
                        originalExpressionStructureString = "(ctg(x))",
                        nameEn = "Cotangent",
                        nameRu = "Котангенс",
                        descriptionShortEn = "Express though sin and cos",
                        descriptionShortRu = "Выразить через sin и cos",
                        descriptionEn = "Express cotangent though sine and cosine by formula",
                        descriptionRu = "Выразить котангенс через синус и косинус по формуле",
                        goalType = "simplification",
                        goalPattern = "!tg,!ctg : ? : ?",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        tags = mutableSetOf(FORMULA_BASE.code),
                        difficulty = 0.1
                ),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(ctg(/(pi;6)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(ctg(/(pi;4)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(ctg(/(pi;3)))", goalPattern = "!cos,!sin,!tg,!ctg,!- : ? : ?"),
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(ctg(/(pi;2)))"),

                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(*(ctg(x);tg(x)))"),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(pi/4))^2 - tg(pi/3) - cos(pi/3) + ctg(pi/6)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(pi/6))^2 + (sin(pi/3))^2 - (tg(pi/3))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(2)))")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("3*(tg(pi/6))^2 - (cos(pi/6))^2 - (cos(pi/3))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(3*sin(x)-cos(x)) / (sin(a) + 2*cos(x))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(tg(x))", rightStructureString = "(5)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((sin(x))^3*(ctg(x))^3*tg(x))/cos(x)^2"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(sin(x))")
                        ),
                        tags = mutableSetOf(TRICK.code, FRACTION.code),
                        difficulty = 0.6
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("tg(pi/8)*ctg(pi/8)*sin(pi/6)*cos(pi/6)*tg(pi/4)*ctg(pi/4)*sin(pi/2)*cos(pi/2)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.8
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(3*(cos(x))^2 + 4*sin(x)*cos(x)) / (2*(cos(x))^2 - 7*(sin(x))^2)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(ctg(x))", rightStructureString = "(+(-(2)))", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(4)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(π/4)/sin(π/4)+ctg(π/4)+tg(π/4)-(tg(pi/3))^2)*ctg(x^2)*tg(x^2)*ctg(pi/6)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.8
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("3*((sin(pi/6))^2+(cos(pi/6))^2+(tg(pi/6))^2+(ctg(pi/6))^2 - (sin(pi/4))^2+(cos(pi/4))^2+(tg(pi/4))^2+(ctg(pi/4))^2 + (sin(pi/3))^2+(cos(pi/3))^2+(tg(pi/3))^2+(ctg(pi/3))^2)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(32)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((cos(π/6) / sin(π/6))^4 - (ctg(π/6))^4) * tg(pi/3)^4 * ctg(pi/3)^4"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.9
                )
        )

        val trigonometricShiftsAndPeriodicity = listOf<TaskITR>(
                basicTrigonometryFormulaSimplificationTask(nameEn = "Sine Shift on π", nameRu = "Сдвиг синуса на π",
                        originalExpressionStructureString = "(sin(+(a;π)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(+(-(sin(a))))"))),
                basicTrigonometryFormulaSimplificationTask(nameEn = "Sine Shift on 2π", nameRu = "Сдвиг синуса на 2π",
                        originalExpressionStructureString = "(sin(+(a;-(*(2;π)))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(sin(a))"))),
                basicTrigonometryFormulaSimplificationTask(nameEn = "Cosine Shift on π", nameRu = "Сдвиг косинуса на π",
                        originalExpressionStructureString = "(cos(+(π;-(a))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(+(-(cos(a))))"))),
                basicTrigonometryFormulaSimplificationTask(nameEn = "Cosine Shift on 2π", nameRu = "Сдвиг косинуса на 2π",
                        originalExpressionStructureString = "(cos(+(a;*(2;π))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(cos(a))"))),
                basicTrigonometryFormulaSimplificationTask(nameEn = "Tangent Shift on π", nameRu = "Сдвиг тангенса на π",
                        originalExpressionStructureString = "(tg(+(a;π)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(tg(a))"))),
                basicTrigonometryFormulaSimplificationTask(nameEn = "Cotangent Shift on π", nameRu = "Сдвиг котангенса на π",
                        originalExpressionStructureString = "(ctg(+(a;-(π))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(ctg(a))"))),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("cos(π/4-2*π)/sin(2*π+π/4)-ctg(π/4-π)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(2*pi + pi/6)+sin(4*pi+pi/4)/cos(3*pi+pi/4)-tg(3*pi/4)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.5)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(10*pi)+sin(9*pi)+sin(8*pi)+sin(7*pi)+sin(6*pi))/(cos(10*pi)+cos(9*pi)+cos(8*pi)+cos(7*pi)+cos(6*pi))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(9*pi/6)+sin(7*pi/6)+sin(5*pi/6)+sin(3*pi/6)+sin(pi/6)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("0.5")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("cos(pi)+cos(7*pi/6)+cos(8*pi/6)+cos(9*pi/6)+cos(10*pi/6)+cos(11*pi/6)+cos(2*pi)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("0")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(ctg(13*pi/4)+ctg(14*pi/4)+tg(15*pi/4)+tg(16*pi/4)+tg(17*pi/4)) / ((tg(13*pi/6))^2 + (tg(14*pi/6))^2 + (ctg(15*pi/6))^2 + (ctg(16*pi/6))^2 + (ctg(17*pi/6))^2)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.15)", "(/(3;20))")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x)*sin(9*π/2))/cos(8*pi+x)-(ctg(x)*tg(x-pi)^2)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg(-7*pi/4)*sin(x)+cos(x-pi)/ctg(x))*(sin(pi/3)+tg(5*pi/3)-cos(7*pi/6))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("3*(ctg(7*pi/3))^2 - (sin(pi/6) + sin(7*pi/3))^2 + 2*sin(pi/3)*sin(13*pi/6)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, SQR_SUM.code, TRICK.code),
                        difficulty = 0.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg(-2*pi/3) + 2*sin(-pi/3)) * (2*sin(2*pi/3) + tg(4*pi/3))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(SHORT_MULTIPLICATION.code, DIFF_SQRS.code, TRICK.code),
                        difficulty = 0.9
                )
        )

        val definitionOfTrigonometryFunctionsCheckYourself = listOf(
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("cos(pi/3) + 2* sin(pi/2) + 4*cos(pi) + (tg(pi/3))^2/3 - 0.5"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(1)))")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(5*cos(x) + 6*sin(x)) / (7*cos(x) - 6*sin(a))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(ctg(x))", rightStructureString = "(2)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(3*(sin(x))^2 + 12*sin(x)*cos(x) + 4*(cos(x))^2) / ((sin(x))^2 + sin(x)*cos(x) - 2*(cos(x))^2)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(tg(x))", rightStructureString = "(2)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(10)")
                        ),
                        tags = mutableSetOf(),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("tg(pi/5)*ctg(pi/5)*sin(pi/3)*cos(pi/3)*tg(pi/3)*ctg(pi/3) - tg(pi/8)*ctg(pi/8)*sin(pi/6)*cos(pi/6)*tg(pi/4)*ctg(pi/4)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code),
                        difficulty = 0.8
                )
        )

        val definitionOfTrigonometryFunctions = degreesAndRadians +
                definitionOfSinCos +
                definitionOfTgCtg +
                trigonometricShiftsAndPeriodicity +
                definitionOfTrigonometryFunctionsCheckYourself


        val pythagoreanIdentity = listOf(
                basicTrigonometryFormulaComputationTask(originalExpressionStructureString = "(+(^(sin(a);2);^(cos(a);2)))"),
                TaskITR(
                        originalExpressionStructureString = "(+(1;-(^(cos(a);2))))",
                        nameEn = "Sin Pythagorean",
                        nameRu = "Пифагоров синус",
                        descriptionShortEn = "Express though sin",
                        descriptionShortRu = "Выразить через sin",
                        descriptionEn = "Express though sine by formula",
                        descriptionRu = "Выразить через синус по формуле",
                        goalType = "simplification",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(sin(a);2))")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        tags = mutableSetOf(FORMULA_BASE.code),
                        difficulty = 0.1
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(1;+(1;^(tg(a);2))))",
                        nameEn = "Pythagorean Tangent",
                        nameRu = "Пифагоров тангенс",
                        descriptionShortEn = "Express though cos",
                        descriptionShortRu = "Выразить через cos",
                        descriptionEn = "Express though cosine by formula",
                        descriptionRu = "Выразить через косинус по формуле",
                        goalType = "simplification",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(cos(a);2))")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        tags = mutableSetOf(FORMULA_BASE.code),
                        difficulty = 0.1
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(1;^(sin(a);2)))",
                        nameEn = "Pythagorean Cotangent",
                        nameRu = "Пифагоров котангенс",
                        descriptionShortEn = "Express though sin",
                        descriptionShortRu = "Выразить через sin",
                        descriptionEn = "Express though sine by formula",
                        descriptionRu = "Выразить через синус по формуле",
                        goalType = "simplification",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(1;^(ctg(a);2)))")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        tags = mutableSetOf(FORMULA_BASE.code),
                        difficulty = 0.1
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(pi/6))^2+(cos(pi/6))^2+1/(sin(pi/6))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(5)")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(1+(tg(7*pi/3))^2-1/(cos(pi/3))^2) * ((sin(pi/3))^2 + ctg(7*pi/3))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code, PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.1
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(1 - sin(x)) / cos(x) - cos(x) / (1 + sin(x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRICK.code, FRACTION.code, PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(1 - (cos(2*x))^2) / (1 - (sin(2*x))^2)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(tg(*(2;x));2))")
                        ),
                        tags = mutableSetOf(TRICK.code, FRACTION.code, PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("1 / (1+(tg(a))^2) + 1 / (1+(ctg(a))^2)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRICK.code, FRACTION.code, PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.8
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(1 - (cos(x))^2) * (tg(x))^2 + 1 - (tg(x))^2"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(cos(x);2))")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(x)*cos(x)*(tg(x)+ctg(x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(5*cos(x) + 6*sin(x)) / (7*cos(x) - 6*sin(a))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(ctg(x))", rightStructureString = "(2)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(FRACTION.code),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((1+sin(x))/(1-sin(x)))^0.5 - ((1-sin(x))/(1+sin(x)))^0.5"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(*(2;tg(x)))")
                        ),
                        domainConditionDescription = "-π/2 < x < 0",
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, FRACTION.code),
                        difficulty = 2.1
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(5*sin(x)-cos(x)) / (2*(sin(x)^3) + 111*(cos(x)^3))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(tg(x))", rightStructureString = "(+(-(4)))", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(21)")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, FRACTION.code, TRICK.code),
                        difficulty = 2.4
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((sin(x))^2 + 5*sin(x)*cos(x) - (cos(x))^2) / (2 - (sin(x))^2 - 2*(cos(x))^2)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(ctg(x))", rightStructureString = "(3)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(23)")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, FRACTION.code, TRICK.code),
                        difficulty = 2.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(x)+sin(x))/(sin(pi/3)*sin(pi/4))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(+(tg(x);ctg(x)))", rightStructureString = "(4)", basedOnTaskContext = true, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "SORTED")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(8)")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, FRACTION.code, TRICK.code),
                        difficulty = 2.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg(x))^4+(ctg(x))^4"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(+(tg(x);-(ctg(x))))", rightStructureString = "(b)", basedOnTaskContext = true, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "SORTED")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(^(b;4);*(4;^(b;2));2))")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, TRICK.code),
                        difficulty = 2.7
                )
//TODO: add 2 tasks, for example on degrees
        )

        val pythagoreanIdentityCheckYourself = listOf(
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(pi/3))^2 + (cos(pi/3))^2 + 1/(cos(pi/3))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(5)")
                        ),
                        tags = mutableSetOf(TRICK.code, FRACTION.code, PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(x)*cos(x)*(tg(x)+ctg(x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code),
                        difficulty = 1.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((1+cos(x))/(1-cos(x)))^0.5 - ((1-cos(x))/(1+cos(x)))^0.5"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(*(2;ctg(x)))")
                        ),
                        domainConditionDescription = "0 < x < π/2",
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, FRACTION.code, TRICK.code),
                        difficulty = 2.1
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(2*(sin(x))^2 - (cos(x))^2) / ((sin(x))^4 + 5*(sin(x))^3*cos(x) - (cos(x))^4)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(ctg(x))", rightStructureString = "(2)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(10)")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, FRACTION.code, TRICK.code),
                        difficulty = 2.4
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg(x))^2+(ctg(x))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(+(tg(x);-(ctg(x))))", rightStructureString = "(b)", basedOnTaskContext = true, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "SORTED")
                        ),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(^(b;2);2))")
                        ),
                        tags = mutableSetOf(PYTHAGOREAN_IDENTITY.code, TRICK.code),
                        difficulty = 2.5
                )
        )

        val trigonometryReflections = listOf(
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Negative Angle Sine", nameRu = "Синус отрицательного угла",
                        originalExpressionStructureString = "(sin(+(-(a))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(+(-(sin(a))))"))),
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Negative Angle Cosine", nameRu = "Косинус отрицательного угла",
                        originalExpressionStructureString = "(cos(+(-(a))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(cos(a))"))),
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Sine Reflected in π/2", nameRu = "Синус отражения в π/2",
                        originalExpressionStructureString = "(sin(+(π;-(a))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(sin(a))"))),
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Cosine Reflected in π/2", nameRu = "Косинус отражения в π/2",
                        originalExpressionStructureString = "(cos(+(π;-(a))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(+(-(cos(a))))"))),
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Sine Reflected to Cosine", nameRu = "Приведение синуса к косинусу",
                        originalExpressionStructureString = "(sin(+(/(π;2);-(a))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(cos(a))"))),
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Cosine Reflected to Sine", nameRu = "Приведение косинуса к синусу",
                        originalExpressionStructureString = "(cos(+(/(π;2);a)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(sin(+(-(a))))"))),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("-cos(-4*pi/3)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.5)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("tg(5*pi/6)*sin(4*pi/3)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.5)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(pi/2-x)-cos(x)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("cos(2*pi/18)-sin(7*pi/18)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("2*tg(-pi/4)*ctg(-pi/6) - 3*sin(-pi/2) - 4*cos(-pi/6)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(3)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.6
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(π/2-x)/cos(x))*(ctg(π/4)-tg(π/4))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(-pi/3)*tg(-pi/6)+3*cos(pi)+cos(-pi/3)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(+(-(2)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(((1-sin(-(π/2-x))^2)^0.5*cos(π)-(cos(-π)*cos(π/2)-sin(-π)*sin(π/2))*cos(π/2-(-(π/2-x))))+cos(π/2-x))*tg(x)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.1
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x+pi)*cos((3*pi)/2-x)*tg(x-pi/2)) / (cos(pi/2+x)*cos((3*pi)/2+x)*tg(pi+x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(ctg(x);2))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code, FRACTION.code),
                        difficulty = 1.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(11*pi/6)*cos(-13*pi/6)*tg(-5*pi/4)*tg(-5*pi/3)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.75)", "(/(3;4))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.7
                )
        )

        val trigonometryReflectionsCheckYourself = listOf(
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(-(22*pi)/6))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.75)", "(/(3;4))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(π/2-x)/sin(x))*(ctg(-π/4)+tg(π/4))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "BasicTrigonometricDefinitionsIdentity")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("6*tg(9*pi/4)+cos(5*pi/6)*tg(-pi/6)-sin(-pi/6)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(7)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code),
                        difficulty = 1.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sqrt(6)/(sin(-7*pi/4)*cos(-7*pi/6)*tg(5*pi/3)*ctg(4*pi/3))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(4)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code, FRACTION.code),
                        difficulty = 1.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x-pi)*cos(x-2*pi)*sin(2*pi-x)) / (sin(pi/2-x)*ctg(pi-x)*ctg(3*pi/2+x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(sin(x);2))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_REFLECTIONS.code, FRACTION.code),
                        difficulty = 1.7
                )
        )

        val trigonometrySinCosAngleSumDiff = listOf(
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Sine of Sum", nameRu = "Синус суммы",
                        originalExpressionStructureString = "(sin(+(a;b)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(+(*(sin(a);cos(b));*(sin(b);cos(a))))"))),
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Sine of Difference", nameRu = "Синус разности",
                        originalExpressionStructureString = "(+(*(sin(a);cos(b));-(*(sin(b);cos(a)))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(sin(+(a;-(b))))"))),


                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Cosine of Sum", nameRu = "Косинус суммы",
                        originalExpressionStructureString = "(+(*(cos(a);cos(b));-(*(sin(b);sin(a)))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(cos(+(a;b)))"))),
                trigonometrySinCosSumReductionFormulaSimplificationTask(nameEn = "Cosine of Difference", nameRu = "Косинус разности",
                        originalExpressionStructureString = "(cos(+(a;-(b))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(+(*(cos(a);cos(b));*(sin(b);sin(a))))"))),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(pi/30)sin(pi/15) + cos(pi/30)cos(pi/15))/((sin(7*pi/30)cos(4*pi/15) + cos(7*pi/30)sin(4*pi/15))*cos(pi/30))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.4
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x+y)-sin(y)*cos(x)) / (sin(x-y)+sin(y)*cos(x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.4
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((cos((11*pi)/20)cos(9*pi/20) - sin((11*pi)/20)sin((9*pi)/20)) / (cos(pi/8)sin((3*pi)/8) - sin(pi/8)cos((3*pi)/8)))^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(pi/6-x)-cos(11*pi/6)*cos(x)) / (sin(pi/6-x)+sin(2*pi/3)*sin(x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(x))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(-pi/8))^4 + (cos(-3*pi/8))^4 + (sin(-5*pi/8))^4 + (cos(-7*pi/8))^4"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1.5)", "(/(3;2))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code),
                        difficulty = 1.5
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(2*(sin(pi/4)*cos(x)-cos(pi/4+x))) / (2*sin(pi/4+x) - sin(x)/sin(pi/4))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(x))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code),
                        difficulty = 1.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg(7*pi/6)+ctg(pi/6))*(sin(5*pi/6)-sin(3*pi/6)*cos(2*pi/6)-sin(2*pi/6)*cos(3*pi/6))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code),
                        difficulty = 1.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(8*x)+sin(2*x)*sin(6*x)) / (cos(2*x)*cos(6*x))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(9*pi/12)*cos(7*pi/12)*tg(5*pi/6)+cos(9*pi/12)*sin(7*pi/12)*tg(-pi/6)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0.5)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.9
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(/(cos(*(6;x));sin(*(3;x)));/(*(sin(*(6;x));cos(*(3;x)));+(/(1;^(sin(*(3;x));2));-(^(sin(*(3;x));2));-(^(ctg(*(3;x));2))))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(/(1;sin(*(3;x))))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.9
                )
        )

        val trigonometrySinCosAngleSumDiffWithDoubleArgs = listOf(
                TaskITR(
                        nameEn = "Double-Angle Sine",
                        nameRu = "Синус двойного угла",
                        originalExpressionStructureString = "(sin(*(2;a)))",
                        goalExpressionStructureString = "(*(2;sin(a);cos(a)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        difficulty = 2.0,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),
                TaskITR(
                        nameEn = "Double-Angle Cosine",
                        nameRu = "Косинус двойного угла",
                        originalExpressionStructureString = "(cos(*(2;a)))",
                        goalExpressionStructureString = "(+(^(cos(a);2);-(^(sin(a);2))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        difficulty = 2.0,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),
                TaskITR(
                        nameEn = "Double-Angle Cosine through Sine",
                        nameRu = "Косинус двойного угла через синус",
                        originalExpressionStructureString = "(+(1;-(*(2;^(sin(a);2)))))",
                        goalExpressionStructureString = "(cos(*(2;a)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "TrigonometrySinCosSumReduction")),
                        difficulty = 2.0,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                )
        ) + listOf(
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(2*x)) / ((cos(x))^2-(sin(x))^2)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(*(2;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.2
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("sin(2*x)*tg(x)+cos(2*x)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(ctg(x)-tg(x))/2"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(ctg(*(2;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("cos(3*x)/(2*sin(x)) + sin(3*x)/(2*cos(x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(ctg(*(2;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.4
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((sin(2*x))^2-4*(sin(x))^2) / ((sin(2*x))^2+4*(sin(x))^2-4)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(tg(x);4))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 2.9
                ),


                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(2*(cos(4*x))^2-1) / (tg(9*pi/4-4*x)*(sin((-3*pi)/4+4*x))^2)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 3.8
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("1/(cos(pi/7)*cos((4*pi)/7)*cos((5*pi)/7))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(8)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code, TRICK.code),
                        difficulty = 4.8,
                        hints = mapOf(
                                "data" to listOf(
                                        HintITR(
                                                "Multiply the numerator and denominator by $$8\\cdot\\sin\\left(\\frac{\\pi}{7}\\right)$$",
                                                "Домножить числитель и знаменатель на $$8\\cdot\\sin\\left(\\frac{\\pi}{7}\\right)$$"
                                        ),
                                        HintITR(
                                                text = "$$\\cos\\left(\\frac{5\\cdot\\pi}{7}\\right)=\\cos\\left(\\pi-\\frac{2\\cdot\\pi}{7}\\right)=-\\cos\\left(\\frac{2\\cdot\\pi}{7}\\right)$$"
                                        ),
                                        HintITR(
                                                "Apply the double angle formula three times$$",
                                                "Трижды применить формулу двойного угла"
                                        )
                                )
                        )
                )
//        , TaskITR(  //TODO: to add such task support quadratic equations
//                originalExpressionStructureString = stringToStructureString("4*sin(pi/10)"),
//                goalExpressionStructureString = stringToStructureString("sqrt(5)-1"),
//                rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
//                tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code, TRICK.code),
//                difficulty = 4.8,
//                hints = mapOf(
//                        "data" to listOf(
//                                HintITR(
//                                        "Use $$\\sin\\left(\\frac{2\\cdot\\pi}{10}\\right)=\\cos\\left(\\frac{3\\cdot\\pi}{10}\\right)$$",
//                                        "Использовать $$\\sin\\left(\\frac{2\\cdot\\pi}{10}\\right)=\\cos\\left(\\frac{3\\cdot\\pi}{10}\\right)$$"
//                                ),
//                                HintITR(
//                                        "Solve the quadratic equation with respect to $$\\sin\\left(\\frac{\\pi}{10}\\right)$$",
//                                        "Решить квадратное уравнение относительно $$\\sin\\left(\\frac{\\pi}{10}\\right)$$"
//                                )
//                        )
//                )
//        )
        )

        val trigonometrySinCosAngleSumDiffWithDoubleArgsCheckYourself = listOf(
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x+pi/4)-cos(x+pi/4)) / (sin(x+pi/4)+cos(x+pi/4))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(x))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 1.4
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(pi/8))^4 + (sin(3*pi/8))^4 + (cos(5*pi/8))^4 + (sin(7*pi/8))^4"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1.5)", "(/(3;2))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code),
                        difficulty = 1.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((sin(2*x))^2-4*(cos(x))^2) / ((sin(2*x))^2+4*(cos(x))^2-4)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(ctg(x);4))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 2.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(1-2*(sin(4*x))^2) / (2*ctg(pi/4+4*x)*(cos((5*pi)/4-4*x))^2)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 3.8
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("1/(sin(pi/18)*cos(pi/9)*cos((2*pi)/9))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(8)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code, TRICK.code),
                        difficulty = 4.8
                )
        )


        val trigonometrySumProdSinCos = listOf(
                //TODO: think about add tasks on such formula deducing
                trigonometrySumProdSinCosFormulaSimplificationTask(nameEn = "Sum of Sine", nameRu = "Сумма синусов",
                        originalExpressionStructureString = "(+(sin(a);sin(b)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(*(2;sin(/(+(a;b);2));cos(/(+(a;-(b));2))))"))),
                trigonometrySumProdSinCosFormulaSimplificationTask(nameEn = "Difference of Sine", nameRu = "Разность синусов",
                        originalExpressionStructureString = "(+(sin(a);-(sin(b))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(*(2;sin(/(+(a;-(b));2));cos(/(+(a;b);2))))"))),

                trigonometrySumProdSinCosFormulaSimplificationTask(nameEn = "Sum of Cosine", nameRu = "Сумма косинусов",
                        originalExpressionStructureString = "(+(cos(a);cos(b)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(*(2;cos(/(+(a;b);2));cos(/(+(a;-(b));2))))"))),
                trigonometrySumProdSinCosFormulaSimplificationTask(nameEn = "Difference of Cosine", nameRu = "Разность косинусов",
                        originalExpressionStructureString = "(+(cos(a);-(cos(b))))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(+(-(*(2;sin(/(+(a;-(b));2));sin(/(+(a;b);2))))))"))),


                trigonometrySumProdSinCosFormulaSimplificationTask(nameEn = "Product of Sine", nameRu = "Произведение синусов",
                        originalExpressionStructureString = "(*(sin(a);sin(b)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(/(+(cos(+(a;-(b)));-(cos(+(a;b))));2))"))),
                trigonometrySumProdSinCosFormulaSimplificationTask(nameEn = "Product of Sine and Cosine", nameRu = "Произведение синуса и косинуса",
                        originalExpressionStructureString = "(*(sin(a);cos(b)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(/(+(sin(+(a;-(b)));sin(+(a;b)));2))"))),
                trigonometrySumProdSinCosFormulaSimplificationTask(nameEn = "Product of Cosine", nameRu = "Произведение косинусов",
                        originalExpressionStructureString = "(*(cos(a);cos(b)))",
                        otherGoalData = mapOf("hiddenGoalExpressions" to listOf("(/(+(cos(+(a;-(b)));cos(+(a;b)));2))"))),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(3*x)-cos(7*x))/(sin(2*x)*sin(5*x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 1.8
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x)+sin(3*x)) / (cos(x)+cos(3*x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(*(2;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 1.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin((5*pi)/18)+cos((2*pi)/18))/(2*cos(pi/18)*sin(pi/3))"),
                        goalType = "computaion",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 2.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(ctg((11*pi)/36)-tg((5*pi)/36))*cos((5*pi)/36)*cos((7*pi)/36)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(sin(/(pi;18)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 2.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("cos(x+(3*pi)/2)*cos(x-(5*pi)/2)+cos(x+(7*pi)/2)*sin(x-9*pi)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 2.0
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(x)-cos(2*x)-cos(4*x)+cos(5*x)) / (sin(x)-sin(2*x)-sin(4*x)+sin(5*x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(ctg(*(3;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 2.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x-pi)*cos(x-2*pi)*sin(2*pi-x)) / (sin(pi/2-x)*ctg((3*pi)/2+x)*ctg(pi-x))"),
                        goalExpressionStructureString = "(^(sin(x);2))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 2.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(x+pi)*cos((3*pi)/2-x)*tg(x-pi/2)) / (cos(pi/2+x)*cos((3*pi)/2+x)*tg(pi+x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(^(ctg(x);2))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 2.6
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(sin(0.3*pi)*cos(-2.8*pi)+cos(0.3*pi)*sin(-2.8*pi)) / (sin(0.3*pi)*sin(-4.3*pi) - cos(0.3*pi)*cos(2.3*pi))"),
                        goalType = "computaion",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 2.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("2*cos(x)-1-cos(2*x)"),
                        goalExpressionStructureString = "(*(4;cos(x);^(sin(/(x;2));2)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 5.3
                ),

                TaskITR(
                        originalExpressionStructureString = stringToStructureString("16*sin((7*pi)/4) * cos((7*pi)/6) * tg((5*pi)/3) * ctg(-(4*pi)/3) * cos(pi/4) * cos(pi/6)"),
                        goalType = "computaion",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(6)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 5.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((cos(4*x-3*pi))^2-4*(cos(2*x-pi))^2+3) / ((cos(4*x+3*pi))^2+4*(cos(2*x+pi))^2-1)"),
                        goalExpressionStructureString = "(^(tg(*(2;x));4))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 6.1
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((sin(x-pi))^2 - 4*(cos(3*pi/2-x/2))^2) / ((cos(x-5*pi/2))^2+4*(cos(pi/2+x/2))^2-4)"),
                        goalExpressionStructureString = "(^(tg(/(x;2));4))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 6.2
                )
        )

        val trigonometrySumProdSinCosCheckYourself = listOf(
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((cos(2*x)-cos(4*x))*ctg(3*x)) / (cos(2*x)+cos(4*x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(x))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 1.9
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg((4*pi)/18)+ctg((2*pi)/18))*cos((7*pi)/18)*cos((4*pi)/18)"),
                        goalExpressionStructureString = "(sin(/(*(11;pi);18)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 2.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("cos(x+pi/2)*cos(3*pi-x)+sin(x+(5*pi)/2)*sin(3*pi+x)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 2.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(cos(x)-cos(3*x)+cos(5*x)-cos(7*x)) / (sin(x)+sin(3*x)+sin(5*x)+sin(7*x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(x))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, FRACTION.code),
                        difficulty = 2.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("16*sin((11*pi)/6) * cos(-(13*pi)/6) * tg(-(5*pi)/4) * ctg(-(5*pi)/3)"),
                        goalType = "computaion",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(4)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, TRIGONOMETRY_PRODUCT.code, FRACTION.code),
                        difficulty = 5.0
                )
        )


        val tgCtgOfSumDiffToSinCosFormulasDeducing = listOf(
                TaskITR(
                        nameEn = "Tangent of Sum",
                        nameRu = "Тангенс суммы",
                        originalExpressionStructureString = "(tg(+(a;b)))",
                        goalExpressionStructureString = "(/(+(tg(a);tg(b));+(1;-(*(tg(a);tg(b))))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 4.5,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),
                TaskITR(
                        nameEn = "Tangent of Difference",
                        nameRu = "Тангенс разности",
                        originalExpressionStructureString = "(/(+(tg(a);-(tg(b)));+(1;*(tg(a);tg(b)))))",
                        goalExpressionStructureString = "(tg(+(a;-(b))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 4.5,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),
                TaskITR(
                        nameEn = "Cotangent of Sum",
                        nameRu = "Котангенс суммы",
                        originalExpressionStructureString = "(/(+(*(ctg(a);ctg(b));-(1));+(ctg(a);ctg(b))))",
                        goalExpressionStructureString = "(ctg(+(a;b)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 4.5,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),
                TaskITR(
                        nameEn = "Cotangent of Difference",
                        nameRu = "Котангенс разности",
                        originalExpressionStructureString = "(ctg(+(a;-(b))))",
                        goalExpressionStructureString = "(/(+(*(ctg(a);ctg(b));1);+(ctg(b);-(ctg(a)))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 4.5,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),

                TaskITR(
                        nameEn = "Double Angle Sine through Tangent",
                        nameRu = "Синус двойного угла через тангенс",
                        originalExpressionStructureString = "(sin(*(2;a)))",
                        goalExpressionStructureString = "(/(*(2;tg(a));+(1;^(tg(a);2))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 4.0,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                ),
                TaskITR(
                        nameEn = "Double Angle Cosine through Tangent",
                        nameRu = "Косинус двойного угла через тангенс",
                        originalExpressionStructureString = "(/(+(1;-(^(tg(a);2)));+(1;^(tg(a);2))))",
                        goalExpressionStructureString = "(cos(*(2;a)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Trigonometry")),
                        difficulty = 4.0,
                        tags = mutableSetOf(FORMULA_DEDUCE.code, TRIGONOMETRY_ANGLE_SUM.code)
                )
        )

        val tgCtgSinCosCompositeTasks = tgCtgOfSumDiffToSinCosFormulasDeducing + listOf(
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(1+tg(pi/24)*tg(13*pi/24)) / (tg(13*pi/24) - tg(pi/24))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 2.0
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(+((^(ctg(/(pi;8));2));-(1));*(2;ctg(/(pi;8)))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 2.0
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("( (tg(5*pi/24) + tg(pi/8)) /  ( (sin(pi/8))^2 + (sin(3*pi/8))^2 - tg(pi/8)*tg(5*pi/24) ) )^2"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(3)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 2.3
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(tg(pi/3+x)*tg(pi/6+5*x) - 1) / (tg(pi/3+x)+ctg(pi/3-5*x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(*(6;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 3.4
                ),
                TaskITR(
                        originalExpressionStructureString = "(/(2;+(tg(+(/(*(5;π);4);x));tg(+(/(*(5;π);4);-(x))))))",
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(cos(*(2;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 3.5
                ),

                TaskITR(
                        originalExpressionStructureString = "(/(+(1;sin(*(2;x));-(cos(*(2;x))));+(1;sin(*(2;x));cos(*(2;x)))))",
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(tg(x))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 5.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("27^0.5/(2*cos((5*pi)/18)) - 6 * sin(pi/18)"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(3)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_REFLECTIONS.code, FRACTION.code),
                        difficulty = 5.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("((cos(5*pi/8+x))^2 - (sin(15*pi/8+x))^2) / cos((7*pi)/4)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(sin(*(2;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 5.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("2*(cos(8*x-3*pi) - 1) / (tg(2*x) - ctg(2*x))"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(sin(*(8;x)))")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 5.5
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("tg(6*x)-tg(4*x)-tg(2*x)"),
                        goalExpressionStructureString = stringToStructureString("tg(6*x)*tg(4*x)*tg(2*x)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        tags = mutableSetOf(TRIGONOMETRY_SUM.code, TRIGONOMETRY_PRODUCT.code),
                        difficulty = 6.4,
                        hints = mapOf(
                                "data" to listOf(
                                        HintITR(
                                                text = "$$ tg\\left(6\\cdot x\\right)-tg\\left(4\\cdot x\\right)=\\frac{\\sin \\left(2\\cdot x\\right)}{\\cos \\left(6\\cdot x\\right)\\cos \\left(4\\cdot x\\right)}$$"
                                        ),
                                        HintITR(
                                                text = "$$ tg\\left(2\\cdot x\\right)=\\frac{\\sin \\left(2\\cdot x\\right)}{\\cos \\left(2\\cdot x\\right)}$$"

                                        )
                                )
                        )
                )
        )


        val tgCtgSinCosCompositeTasksCheckYourself = listOf(
                TaskITR(
                        originalExpressionStructureString = "(/(+(*(ctg(*(13;/(π;8)));ctg(*(3;/(π;8))));^(cos(/(π;8));2);^(cos(*(5;/(π;8)));2));+(ctg(*(3;/(π;8)));-(ctg(*(13;/(π;8)))))))",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(1)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 2.3
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("(1 - tg(pi/4+x)*tg(pi/4+3*x)) / (tg(pi/4+x)+ctg(pi/4-3*x)) + tg(4*x)"),
                        goalType = "simplification",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 3.4
                ),
                TaskITR(
                        originalExpressionStructureString = "(*(+(sin(*(4;x));*(cos(*(4;x));ctg(*(2;x)))));100)",
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(tg(*(2;x)))", rightStructureString = "(5)", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 2, code = "", normalizationType = "ORIGINAL")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_PRODUCT.code, TRIGONOMETRY_ANGLE_SUM.code),
                        difficulty = 0.7
                ),
                TaskITR(
                        originalExpressionStructureString = stringToStructureString("2*(1/(2*cos(pi*4/9)) - 2* sin(pi*7/18))"),
                        goalType = "computation",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(2)")
                        ),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, TRIGONOMETRY_REFLECTIONS.code, FRACTION.code),
                        difficulty = 5.5
                ),
                TaskITR(
                        originalExpressionStructureString = "(+(/(1;(+(tg(*(3;a));tg(a))));-(/(1;(+(ctg(*(3;a));ctg(a)))))))",
                        goalExpressionStructureString = "(ctg(*(4;a)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry")),
                        tags = mutableSetOf(TRIGONOMETRY_ANGLE_SUM.code, FRACTION.code),
                        difficulty = 5.5
                )
        )


        val trigonometryStepByStepTasks = definitionOfTrigonometryFunctions +
                pythagoreanIdentity + pythagoreanIdentityCheckYourself +
                trigonometryReflections + trigonometryReflectionsCheckYourself +
                trigonometrySinCosAngleSumDiff + trigonometrySinCosAngleSumDiffWithDoubleArgs + trigonometrySinCosAngleSumDiffWithDoubleArgsCheckYourself +
                trigonometrySumProdSinCos + trigonometrySumProdSinCosCheckYourself +
                tgCtgSinCosCompositeTasks + tgCtgSinCosCompositeTasksCheckYourself


//1. Definition of trigonometry functions
//  - градусные и радианные меры углов
//  - значения тригонометрических функций в основных углах
//  - отношения между тригонометрическими функциями по определению (tg = sin / cos; ctg = cos / sin)
//  - периоды функций (приведение с разницей до pi и 2*pi)

//2. Основное тригонометрическое тождество

//3. Формулы приведения
//  - sin к cos и обратно

//4. Формулы суммы и разности sin и cos
//  - Формулы двойного угла

//5. Преобразование суммы в произведение и обратно

//6. Формулы суммы и разности tg и ctg
//  - Формулы двойного угла
//  - приведение sin и cos к tg и ctg и обратно

//7. Обратные тригонометрические функции
//  -- определение
//  -- задачи на выражение (уравнения)

    }
}