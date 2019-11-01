package expressionstests

import com.twf.expressiontree.ExpressionTreeParser
import org.junit.Test
import kotlin.test.assertEquals

class ParsedExpressionsPositionsTests{


    @Test
    fun testSinPlus (){
        val expressionTreeParser = ExpressionTreeParser("sin(2*x) + sin(x+sin(y))", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(sin(*(2{4;5};x{6;7}){3;7}){0;7};sin(+(x{15;16};sin(y{21;22}){17;22}){14;23}){11;23}){0;24}){0;24}", root.toStringWithPositions())
    }
}