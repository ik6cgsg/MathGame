package mathhelper.twf.defaultcontent.defaultrulepacks

import mathhelper.twf.config.RuleITR
import mathhelper.twf.config.RulePackITR
import mathhelper.twf.config.RulePackLinkITR

class DefaultCombinatoricsRulePacks {
    companion object {
        val defaultCombinatoricsRulePacks = listOf(
                RulePackITR(
                        code = "Factorial",
                        nameEn = "Factorial", nameRu = "Факториал",
                        descriptionShortEn = "Basic Properties of Natural Factorial", descriptionShortRu = "Основные свойства натурального факториала",
                        subjectType = "combinatorics",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "ShortMultiplication")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(factorial(+(n;1)))", rightStructureString = "(*(factorial(n);+(n;1)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(*(factorial(n);+(n;1)))", rightStructureString = "(factorial(+(n;1)))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 35, code = "", normalizationType = "SORTED"),
                                RuleITR(leftStructureString = "(/(factorial(+(n;1));+(n;1)))", rightStructureString = "(factorial(n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(factorial(n))", rightStructureString = "(/(factorial(+(n;1));+(n;1)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(/(factorial(+(n;1));factorial(n)))", rightStructureString = "(+(n;1))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(+(n;1))", rightStructureString = "(/(factorial(+(n;1));factorial(n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(factorial(n))", rightStructureString = "(*(factorial(+(n;-(1)));n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(*(factorial(+(n;-(1)));n))", rightStructureString = "(factorial(n))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 35, code = "", normalizationType = "SORTED"),
                                RuleITR(leftStructureString = "(/(factorial(n);n))", rightStructureString = "(factorial(+(n;-(1))))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(factorial(+(n;-(1))))", rightStructureString = "(/(factorial(n);n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(/(factorial(n);factorial(+(n;-(1)))))", rightStructureString = "(n)", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(n)", rightStructureString = "(/(factorial(n);factorial(+(n;-(1)))))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 85, code = "")
                        )
                ),
                RulePackITR(
                        code = "BasicCombinatorics",
                        nameEn = "Basic Combinatorics", nameRu = "Базовая комбинаторика",
                        descriptionShortEn = "Connections between Base Combinatorial Numbers", descriptionShortRu = "Формулы, связывающие основные комбинаторные числа",
                        descriptionEn = "Simple formulas for Placements (A), Placements with Repetitions (U), Combinations (C), Combinations with repetitions (V), Permutations (P), Stirling I kind (S1), Stirling II kind (S2), Bell (B), Catalan (C)", descriptionRu = "Простые формулы для чисел Размещения (A), Размещения с повторениями (U), Сочетаний (C), Сочетаний с повторениями (V), Перестановок (P), Стирлинга I рода (S1), Стирлинга II рода (S2), Белла (B), Каталана (C)",
                        subjectType = "combinatorics",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "Factorial")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(U(m;n))", rightStructureString = "(^(m;n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(^(m;n))", rightStructureString = "(U(m;n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(P(n))", rightStructureString = "(factorial(n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(factorial(n))", rightStructureString = "(P(n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(A(n;n))", rightStructureString = "(P(n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(P(n))", rightStructureString = "(A(n;n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 70, code = ""),
                                RuleITR(leftStructureString = "(*(C(m;n);P(n)))", rightStructureString = "(A(m;n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(A(m;n))", rightStructureString = "(*(C(m;n);P(n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(/(A(m;n);P(n)))", rightStructureString = "(C(m;n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(C(m;n))", rightStructureString = "(/(A(m;n);P(n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(/(A(m;n);C(m;n)))", rightStructureString = "(P(n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(P(n))", rightStructureString = "(/(A(m;n);C(m;n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(V(+(m;1);n))", rightStructureString = "(/(factorial(+(m;n));*(factorial(m);factorial(n))))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(/(factorial(+(m;n));*(factorial(m);factorial(n))))", rightStructureString = "(V(+(m;1);n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(factorial(+(m;n)))", rightStructureString = "(*(V(+(m;1);n);*(factorial(m);factorial(n))))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(*(V(+(m;1);n);*(factorial(m);factorial(n))))", rightStructureString = "(factorial(+(m;n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(*(factorial(m);factorial(n)))", rightStructureString = "(/(factorial(+(m;n));V(+(m;1);n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(/(factorial(+(m;n));V(+(m;1);n)))", rightStructureString = "(*(factorial(m);factorial(n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(C(+(m;n);n))", rightStructureString = "(/(factorial(+(m;n));*(factorial(m);factorial(n))))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(C(+(m;n);n))", rightStructureString = "(V(+(m;1);n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(V(+(m;1);n))", rightStructureString = "(C(+(m;n);n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(A(+(m;n);n))", rightStructureString = "(/(factorial(+(m;n));factorial(m)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(/(factorial(+(m;n));factorial(m)))", rightStructureString = "(A(+(m;n);n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(factorial(+(m;n)))", rightStructureString = "(*(A(+(m;n);n);factorial(m)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(*(A(+(m;n);n);factorial(m)))", rightStructureString = "(factorial(+(m;n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(*(factorial(m);A(+(m;n);n)))", rightStructureString = "(factorial(+(m;n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(factorial(m))", rightStructureString = "(/(factorial(+(m;n));A(+(m;n);n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(/(factorial(+(m;n));A(+(m;n);n)))", rightStructureString = "(factorial(m))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(S1(m;n))", rightStructureString = "(*(factorial(n);S2(m;n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(*(factorial(n);S2(m;n)))", rightStructureString = "(S1(m;n))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 35, code = "", normalizationType = "SORTED"),
                                RuleITR(leftStructureString = "(factorial(n))", rightStructureString = "(/(S1(m;n);S2(m;n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(/(S1(m;n);S2(m;n)))", rightStructureString = "(factorial(n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(S2(m;n))", rightStructureString = "(/(S1(m;n);factorial(n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(/(S1(m;n);factorial(n)))", rightStructureString = "(S2(m;n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(S2(+(m;1);+(n;1)))", rightStructureString = "(+(S2(m;n);*(n;S2(m;+(n;1)))))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(+(S2(m;n);*(n;S2(m;+(n;1)))))", rightStructureString = "(S2(+(m;1);+(n;1)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(S2(m;m))", rightStructureString = "(1)", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(1)", rightStructureString = "(S2(m;n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 90, code = ""),
                                RuleITR(leftStructureString = "(C(*(2;n);n))", rightStructureString = "(*(C(n);+(n;1)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(*(C(n);+(n;1)))", rightStructureString = "(C(*(2;n);n))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 35, code = "", normalizationType = "SORTED"),
                                RuleITR(leftStructureString = "(C(n))", rightStructureString = "(/(C(*(2;n);n);+(n;1)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(/(C(*(2;n);n);+(n;1)))", rightStructureString = "(C(n))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(+(n;1))", rightStructureString = "(/(C(*(2;n);n);C(n)))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 80, code = ""),
                                RuleITR(leftStructureString = "(/(C(*(2;n);n);C(n)))", rightStructureString = "(+(n;1))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 35, code = ""),
                                RuleITR(leftStructureString = "(C(0))", rightStructureString = "(1)", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 5, code = ""),
                                RuleITR(leftStructureString = "(1)", rightStructureString = "(C(0))", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = true, priority = 90, code = "")
                        )
                )
        )

        fun get() = defaultCombinatoricsRulePacks
    }
}