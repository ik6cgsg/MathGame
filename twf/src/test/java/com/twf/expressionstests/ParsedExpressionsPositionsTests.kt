package com.twf.expressionstests

import com.twf.expressiontree.ExpressionTreeParser
import com.twf.org.junit.Test
import kotlin.test.assertEquals

class ParsedExpressionsPositionsTests{


    @Test
    fun testSinPlus (){
        val expressionTreeParser = ExpressionTreeParser("sin(2*x) + sin(x+sin(y))", true)
        expressionTreeParser.parse()
        val root = expressionTreeParser.root
        assertEquals("(+(sin(*(2{4;5};x{6;7}){3;8}){0;8};sin(+(x{15;16};sin(y{21;22}){17;23}){14;24}){11;24}){0;24}){0;24}", root.toStringWithPositions())
    }
}