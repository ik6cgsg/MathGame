package mathhelper.twf.defaultcontent.defaulttasksets

import mathhelper.twf.config.RulePackLinkITR
import mathhelper.twf.config.TaskITR
import mathhelper.twf.config.TaskSetITR
import mathhelper.twf.defaultcontent.TaskTagCode.*

class LogicTaskSets {
    companion object {
        val logicBaseTrainSetTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(and(or(A;not(B));or(A;not(C))))",
                        goalExpressionStructureString = "(or(A;not(or(B;C))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase")),
                        subjectType = "logic",
                        difficulty = 1.0,
                        stepsNumber = 2,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(or(A;and(A;B)))",
                        goalExpressionStructureString = "(A)",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase")),
                        subjectType = "logic",
                        difficulty = 2.5,
                        tags = mutableSetOf(LOGIC.code, TRICK.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(implic(implic(A;not(B));C))",
                        goalExpressionStructureString = "(or(and(A;B);C))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 2.0,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(implic(and(A;B);or(not(C);B)))",
                        goalExpressionStructureString = "(implic(A;implic(B;implic(C;B))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 3.0,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(and(or(A;B;C);implic(A;or(C;D));implic(C;or(B;D));not(B);not(D)))",
                        goalType = "simplification",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 5.0,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(and(or(A;B);implic(not(C);not(A));implic(not(D);not(B));not(or(C;D));not(or(not(C);not(D)))))",
                        goalType = "simplification",
                        otherGoalData = mapOf(
                                "hiddenGoalExpressions" to listOf("(0)")
                        ),
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 5.5,
                        tags = mutableSetOf(LOGIC.code)
                )
        )

        val logicRelativeComplementTrainSetTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(or(set-(not(A);B);C))",
                        goalExpressionStructureString = "(implic(or(A;B);C))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase")),
                        subjectType = "logic",
                        difficulty = 2.0,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(set-(not(and(A;B));B))",
                        goalExpressionStructureString = "(not(B))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 2.0,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(or(set-(set-(A;B);C);or(not(B);C)))",
                        goalExpressionStructureString = "(implic(implic(A;or(B;C));implic(B;C)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "RelativeComplement"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 4.5,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(implic(or(A;B);and(A;B)))",
                        goalExpressionStructureString = "(and(not(set-(A;B));not(set-(B;A))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "RelativeComplement"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 3.5,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(implic(or(A;B);and(A;B)))",
                        goalExpressionStructureString = "(and(not(set-(A;B));not(set-(B;A))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "RelativeComplement"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 3.5,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(set-(or(A;B);and(implic(A;B);implic(B;A))))",
                        goalExpressionStructureString = "(or(set-(A;B);set-(B;A)))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "RelativeComplement"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 4.0,
                        tags = mutableSetOf(LOGIC.code)
                ),
                TaskITR(
                        originalExpressionStructureString = "(implic(implic(set-(A;C);D);set-(D;B)))",
                        goalExpressionStructureString = "(or(set-(D;B);set-(A;or(C;D))))",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "RelativeComplement"), RulePackLinkITR(rulePackCode = "LogicAbsorptionLaw")),
                        subjectType = "logic",
                        difficulty = 4.5,
                        tags = mutableSetOf(LOGIC.code)
                )
        )

        val logicNormalFormsTrainSetTasks = listOf<TaskITR>(
                TaskITR(
                        originalExpressionStructureString = "(set-(A;set-(A;B)))",
                        goalPattern = "and : (or) : : : not",
                        goalType = "CNF",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "LogicBase")),
                        subjectType = "logic",
                        difficulty = 1.5,
                        tags = mutableSetOf(LOGIC.code, NORMAL_FORMS.code, CNF.code)
                )
        )

        val allLogicTasks = logicBaseTrainSetTasks +
                logicRelativeComplementTrainSetTasks

        val defaultLogicTaskSets = listOf(
                TaskSetITR(
                        code = "LogicBaseTrainSet",
                        nameEn = "[Train Set] Basic Boolean Logic", nameRu = "[Тренировка] Основы булевой логики",
                        descriptionShortEn = "Logic Expressions Transformations",
                        descriptionShortRu = "Преобразования логических выражений",
                        descriptionEn = "Conjunction, Disjunction, Implication, de Morgan's laws, Absorption",
                        descriptionRu = "Конъюнкция, дизъюнкция, импликация, законы де Моргана, поглощение",
                        subjectType = "logic",
                        tasks = logicBaseTrainSetTasks.map { it.copy() }
                )
        )
    }
}