package mathhelper.twf.defaultcontent.defaultrulepacks

import mathhelper.twf.config.RuleITR
import mathhelper.twf.config.RulePackITR
import mathhelper.twf.config.RulePackLinkITR

class DefaultComplexRulePacks {
    companion object {
        val defaultComplexRulePacks = listOf(
                RulePackITR(
                        code = "ComplexNumbers",
                        nameEn = "Complex Numbers", nameRu = "Комплексные числа",
                        descriptionShortEn = "Basic properties", descriptionShortRu = "Основные свойства",
                        subjectType = "complex_numbers",
                        rulePacks = listOf(RulePackLinkITR(rulePackCode = "AdvancedTrigonometry"), RulePackLinkITR(rulePackCode = "Logarithm")),
                        rules = listOf(
                                RuleITR(leftStructureString = "(*(sys_def_i_complex;sys_def_i_complex))", rightStructureString = "(+(-(1)))", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 100, code = ""),
                                RuleITR(leftStructureString = "(^(sys_def_i_complex;2))", rightStructureString = "(+(-(1)))", basedOnTaskContext = true, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 100, code = ""),
                                RuleITR(leftStructureString = "(abs(+(x;*(y;sys_def_i_complex))))", rightStructureString = "(sqrt(+(^(x;2);^(y;2))))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 30, code = "", normalizationType = "SORTED_AND_I_MULTIPLICATED"),
                                RuleITR(leftStructureString = "(abs(+(x;*(y;sys_def_i_complex))))", rightStructureString = "(^(+(^(x;2);^(y;2));0.5))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 30, code = "", normalizationType = "SORTED_AND_I_MULTIPLICATED"),
                                RuleITR(leftStructureString = "(exp(+(x;*(y;sys_def_i_complex))))", rightStructureString = "(*(exp(x);+(cos(y);*(sys_def_i_complex;sin(y)))))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 30, code = "", normalizationType = "SORTED_AND_I_MULTIPLICATED"),
                                RuleITR(leftStructureString = "(exp(ln(a)))", rightStructureString = "(a)", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 30, code = ""),
                                RuleITR(leftStructureString = "(cos(z))", rightStructureString = "(/(+(exp(*(sys_def_i_complex;z));exp(+(-(*(sys_def_i_complex;z)))));2))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 30, code = ""),
                                RuleITR(leftStructureString = "(sin(z))", rightStructureString = "(/(+(exp(*(sys_def_i_complex;z));-(exp(+(-(*(sys_def_i_complex;z))))));*(2;sys_def_i_complex)))", basedOnTaskContext = false, matchJumbledAndNested = true, simpleAdditional = false, isExtending = false, priority = 30, code = ""),
                                RuleITR(leftStructureString = "(^(e;ln(a)))", rightStructureString = "(a)", basedOnTaskContext = false, matchJumbledAndNested = false, simpleAdditional = false, isExtending = false, priority = 30, code = "")
                        )
                ))

        fun get() = defaultComplexRulePacks
    }
}