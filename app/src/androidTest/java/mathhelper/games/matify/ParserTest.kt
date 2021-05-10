package mathhelper.games.matify

import androidx.test.ext.junit.runners.AndroidJUnit4
import mathhelper.games.matify.game.Rule
import mathhelper.games.matify.game.RulePackage
import mathhelper.games.matify.parser.GsonParser
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParserTest {
    /** RULES **/

    @Test
    fun ruleShortOk() {
        val rule = """
        {
            "code": "test",
            "nameEn": "test",
            "leftStructureString": "test",
            "rightStructureString": "test"
        }
        """.trimIndent()
        val actual = GsonParser.parse<Rule>(rule)
        val expected = Rule(
            code = "test",
            nameEn = "test",
            leftStructureString = "test",
            rightStructureString = "test")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun ruleNoRequired() {
        val rule = """
        {
            "nameEn": "test",
            "leftStructureString": "test",
            "rightStructureString": "test"
        }
        """.trimIndent()
        val actual = GsonParser.parse<Rule>(rule)
        Assert.assertEquals(null, actual)
    }

    @Test
    fun ruleBadJson() {
        val rule = """
        {
            "nameEn": "test"
            "leftStructureStrin": "test"
            "rightStructureString": "test"
        
        """.trimIndent()
        val actual = GsonParser.parse<Rule>(rule)
        Assert.assertEquals(null, actual)
    }

    @Test
    fun ruleLongOk() {
        val rule = """
        {
            "code": "and_commutativity",
            "nameEn": "And Commutativity",
            "nameRu": "И коммутативное",
            "descriptionShortEn": "Logic mult commutativity",
            "descriptionShortRu": "Коммутативность логического умножения",
            "descriptionEn": "Some long english text",
            "descriptionRu": "Следует из закона о ...",
            "leftStructureString": "(and(a;b))",
            "rightStructureString": "(and(b;a))",
            "basedOnTaskContext": true,
            "matchJumbledAndNested": true,
            "isExtending": true,
            "simpleAdditional": true,
            "priority": 50,
            "normalizationType": "Original",
            "weight": 0.7
        }
        """.trimIndent()
        val actual = GsonParser.parse<Rule>(rule)
        val expected = Rule(
            code = "and_commutativity",
            nameEn = "And Commutativity",
            nameRu = "И коммутативное",
            descriptionShortEn = "Logic mult commutativity",
            descriptionShortRu = "Коммутативность логического умножения",
            descriptionEn = "Some long english text",
            descriptionRu = "Следует из закона о ...",
            leftStructureString = "(and(a;b))",
            rightStructureString = "(and(b;a))",
            basedOnTaskContext = true,
            matchJumbledAndNested = true,
            isExtending = true,
            simpleAdditional = true,
            priority = 50,
            normalizationType = "Original",
            weight = 0.7
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun ruleShortBoolAsString() {
        val rule = """
        {
            "code": "test",
            "nameEn": "test",
            "leftStructureString": "test",
            "rightStructureString": "test",
            "basedOnTaskContext": "true",
        }
        """.trimIndent()
        val actual = GsonParser.parse<Rule>(rule)
        val expected = null
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun ruleShortDoubleWithComma() {
        val rule = """
        {
            "code": "test",
            "nameEn": "test",
            "leftStructureString": "test",
            "rightStructureString": "test",
            "weight": 0,7
        }
        """.trimIndent()
        val actual = GsonParser.parse<Rule>(rule)
        val expected = null
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun rulePackShortOk() {
        val rulePack = """
        {
            "code": "test",
            "namespaceCode": "test",
            "version": 1,
            "nameEn": "test"
        }
        """.trimIndent()
        val actual = GsonParser.parse<RulePackage>(rulePack)
        val expected = RulePackage(
            code = "test",
            namespaceCode = "test",
            version = 1,
            nameEn = "test",
        )
        Assert.assertEquals(expected, actual)
    }
}