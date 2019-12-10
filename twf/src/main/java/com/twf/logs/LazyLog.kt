package com.twf.logs

import com.twf.expressiontree.ExpressionNode
import com.twf.expressiontree.ExpressionSubstitution
import com.twf.factstransformations.ComparableTransformationPartType
import com.twf.factstransformations.FactConstructorViewer
import com.twf.factstransformations.FactSubstitution
import com.twf.factstransformations.MainChainPart
import com.twf.platformdependent.toStringWithMinLength

enum class MessageType { USER, TECHNICAL, ALL }

class LazyMessage(message: () -> String,
                  val messageType: MessageType,
                  val level: Int) {
    val message: String by lazy { message.invoke() }
}

class LazyLog {
    var factConstructorViewer: FactConstructorViewer = FactConstructorViewer()
    val log = mutableListOf<LazyMessage>()
    var currentLevel = 0

    fun addMessage(message: () -> String,
                   messageType: MessageType = MessageType.TECHNICAL,
                   level: Int? = null,
                   levelChange: Int = 0) {
        changeLevel(level, levelChange)
        log.add(LazyMessage(message, messageType, currentLevel))
    }

    /**
     * Add the message with the two fact messages on the new lines (otherwise it will be looked bad).
     * First fact messages is for human reading, second is for computer reading (for inserting into test)
     * Second message type is always technical.
     */
    fun addMessageWithFactDetail(message: () -> String,
                                 fact: MainChainPart,
                                 messageType: MessageType = MessageType.TECHNICAL,
                                 level: Int? = null,
                                 levelChange: Int = 0) {
        changeLevel(level, levelChange)
        log.add(LazyMessage(message, messageType, currentLevel))
        log.add(LazyMessage({ factToUserString(fact) }, messageType, currentLevel + 1))
        log.add(LazyMessage({ factToTechnicalString(fact) }, MessageType.TECHNICAL, currentLevel + 1))
    }

    /**
     * Add the message with the fact message for human reading on the end (otherwise it will be looked bad).
     */
    fun addMessageWithFactShort(message: () -> String,
                                fact: MainChainPart,
                                messageType: MessageType = MessageType.TECHNICAL,
                                level: Int? = null,
                                levelChange: Int = 0) {
        changeLevel(level, levelChange)
        log.add(LazyMessage({ message.invoke() + "'${factToUserString(fact)}'" }, messageType, currentLevel))
    }

    fun addMessageWithFactSubstitutionDetail(message: () -> String,
                                             substitution: FactSubstitution,
                                             messageType: MessageType = MessageType.TECHNICAL,
                                             level: Int? = null,
                                             levelChange: Int = 0) {
        changeLevel(level, levelChange)
        log.add(LazyMessage(message, messageType, currentLevel))
        addMessageWithFactDetail({ "Left: " }, substitution.left, messageType)
        addMessageWithFactDetail({ "Right: " }, substitution.right, messageType)
    }

    /**
     * Add the message with the fact substitution message for human reading on the end (otherwise it will be looked bad).
     */
    fun addMessageWithFactSubstitutionShort(message: () -> String,
                                            substitution: FactSubstitution,
                                            messageType: MessageType = MessageType.TECHNICAL,
                                            level: Int? = null,
                                            levelChange: Int = 0) {
        changeLevel(level, levelChange)
        log.add(LazyMessage({ message.invoke() + "'${factToUserString(substitution.left)}' -> '${factToUserString(substitution.right)}'" },
                messageType, currentLevel))
    }

    /**
     * Add the message with the expression message on the end (otherwise it will be looked bad).
     */
    fun addMessageWithExpression(message: () -> String,
                                 expressionNode: com.twf.expressiontree.ExpressionNode,
                                 messageType: MessageType = MessageType.TECHNICAL,
                                 level: Int? = null,
                                 levelChange: Int = 0) {
        changeLevel(level, levelChange)
        log.add(LazyMessage({ message.invoke() + "'${expressionToString(expressionNode)}'" }, messageType, currentLevel))
    }

    /**
     * Add the message with the expression substitution message for human reading on the end (otherwise it will be looked bad).
     */
    fun addMessageWithExpressionSubstitutionShort(message: () -> String,
                                                  substitution: ExpressionSubstitution,
                                                  messageType: MessageType = MessageType.TECHNICAL,
                                                  level: Int? = null,
                                                  levelChange: Int = 0) {
        changeLevel(level, levelChange)
        log.add(LazyMessage({ message.invoke() + "'${expressionToString(substitution.left)}' -> '${expressionToString(substitution.right)}'" },
                messageType, currentLevel))
    }

    fun changeLevel(newLevel: Int? = null,
                    levelChange: Int = 0) {
        currentLevel = newLevel ?: currentLevel + levelChange
    }

    fun getLogInPlainText(messageType: MessageType = MessageType.ALL, maxLevel: Int = Int.MAX_VALUE,
                          returnLogWithLogLevel: Boolean = true,
                          logLevelPrefixInPlainText: String = "LL_",
                          levelChangeShift: String = "|   ",
                          suffixChar: Char = ' '): String {
        val result = StringBuilder("Log type = '${messageType.toString()}', max message level = '$maxLevel'\n")
        val messages = filterMessages(messageType, maxLevel)
        val maxLevelNumberLength = maxLevel.toString().length
        for (message in messages) {
            if (returnLogWithLogLevel) {
                result.append(logLevelPrefixInPlainText)
                result.append(message.level.toStringWithMinLength(maxLevelNumberLength, ' '))
            }
            result.append(levelChangeShift.repeat(message.level))
            result.append(message.message)
            result.append('\n')
        }
        return result.toString()
    }

    fun getLogInJson(messageType: MessageType = MessageType.ALL, maxLevel: Int = Int.MAX_VALUE): String {
        val result = StringBuilder("{\"logType_${messageType}__MaxMessageLevel_$maxLevel\":[")
        val messages = filterMessages(messageType, maxLevel)
        var currentLevel = 0
        for (message in messages) {
            while (message.level > currentLevel) {
                result.append("{\"message\":\"root\", \"details\":[")
                currentLevel++
            }
            while (message.level < currentLevel) {
                result.append("]}")
                currentLevel--
            }
            if (message.level == currentLevel) {
                if (result.last() == '}') {
                    result.append(',')
                }
                result.append("{\"message\":\"${message.message.replace('\"', '\'')}\", \"details\":[")
                currentLevel++
            }
        }
        while (0 < currentLevel) {
            result.append("]}")
            currentLevel--
        }
        result.append("]}")
        return result.toString()
    }

    fun filterMessages(messageType: MessageType, maxLevel: Int) = when (messageType) {
        MessageType.ALL -> log
        else -> log.filter { it.messageType == messageType }
    }.filter { it.level <= maxLevel }

    fun clear() {
        currentLevel = 0
        log.clear()
    }

    private fun factToUserString(fact: MainChainPart) =
            factConstructorViewer.constructFactUserView(fact)

    private fun factToTechnicalString(fact: MainChainPart) =
            factConstructorViewer.constructIdentifierByFact(fact)

    /**
     * expression user and technical strings are the same
     */
    private fun expressionToString(expressionNode: ExpressionNode) = expressionNode.toString()

    fun add(value: Any, prefix: () -> String, suffix: () -> String, level: Int? = null, levelChange: Int = 0, messageType: MessageType = MessageType.TECHNICAL) {
        addMessage({ "${prefix.invoke()}$value${suffix.invoke()}" }, level = level, levelChange = levelChange, messageType = messageType)
    }

    fun add(value1: Any, value2: Any, prefix: () -> String, middle: () -> String, suffix: () -> String, level: Int? = null, levelChange: Int = 0, messageType: MessageType = MessageType.TECHNICAL) {
        addMessage({ "${prefix.invoke()}$value1${middle.invoke()}$value2${suffix.invoke()}" }, level = level, levelChange = levelChange, messageType = messageType)
    }

    fun add(value1: Any, value2: Any, value3: Any, prefix: () -> String, middle1: () -> String, middle2: () -> String, suffix: () -> String, level: Int? = null, levelChange: Int = 0, messageType: MessageType = MessageType.TECHNICAL) {
        addMessage({ "${prefix.invoke()}$value1${middle1.invoke()}$value2${middle2.invoke()}$value3${suffix.invoke()}" }, level = level, levelChange = levelChange, messageType = messageType)
    }

    /**
     * returns first argument and com.twf.logs it's value; should be used in assigns
     */
    fun <T> assignAndLog(data: T, logLevel: Int, variableName: () -> String): T {
        addMessage({ "${variableName.invoke()} = '${data.toString()}'" }, level = logLevel)
        return data
    }

    fun logCheckParams(onExpressionLevel: Boolean? = null,
                       factsTransformations: List<FactSubstitution>? = null,
                       expressionTransformations: List<ExpressionSubstitution>? = null,
                       additionalFacts: List<MainChainPart>? = null,
                       messageType: MessageType = MessageType.TECHNICAL) {
        val originalLogLevel = currentLevel
        addMessage({ "function parameters:" }, messageType)
        currentLevel++
        addMessage({ "onExpressionLevel: '$onExpressionLevel'" }, messageType)
        addMessage({ "factsTransformations" }, messageType, level = originalLogLevel + 1)
        var i = 0
        if (factsTransformations != null) {
            for (factsTransformation in factsTransformations) {
                val j = i
                addMessageWithFactSubstitutionDetail({ "$j. ${factsTransformation.name}:" }, factsTransformation, messageType, level = originalLogLevel + 2)
                i++
            }
        }
        addMessage({ "expressionTransformations" }, level = originalLogLevel + 1)
        if (expressionTransformations != null) {
            i = 0
            for (expressionTransformation in expressionTransformations) {
                val j = i
                addMessageWithExpressionSubstitutionShort({ "$j. ${expressionTransformation.name}:" }, expressionTransformation, messageType, level = originalLogLevel + 2)
                i++
            }
        }
        addMessage({ "additionalFacts" }, level = originalLogLevel + 1)
        if (additionalFacts != null) {
            i = 0
            for (additionalFact in additionalFacts) {
                val j = i
                addMessageWithFactDetail({ "$j. " }, additionalFact, messageType, level = originalLogLevel + 2)
                i++
            }
        }
        currentLevel = originalLogLevel
    }

    fun logSystemFacts (type: ComparableTransformationPartType, facts: List<MainChainPart>?, message: () -> String, messageType: MessageType = MessageType.TECHNICAL){
        val originalLogLevel = currentLevel
        if (type == ComparableTransformationPartType.MAIN_LINE_AND_NODE) {
            addMessage({ "${message.invoke()} AND system facts:" }, messageType)
        } else {
            addMessage({ "${message.invoke()} OR system facts:" }, messageType)
        }
        if (facts != null) {
            var i = 0
            for (additionalFact in facts) {
                val j = i
                addMessageWithFactDetail({ "$j. " }, additionalFact, messageType, level = originalLogLevel + 1)
                i++
            }
        }
        currentLevel = originalLogLevel
    }

    fun logFactsCompareAsIsParams (left: MainChainPart, right: MainChainPart,
                                   additionalFacts: List<MainChainPart>,
                                   maxTransformationWeight: Double, maxBustCount: Int,
                                   minPossibleTransformationWeight: Double, additionalFactUsed: MutableList<Boolean>, messageType: MessageType = MessageType.TECHNICAL){
        val originalLogLevel = currentLevel
        addMessageWithFactDetail({"Left fact: "}, left, messageType, originalLogLevel + 1)
        addMessageWithFactDetail({"Right fact: "}, right, messageType, originalLogLevel + 1)
        var i = 0
        for (additionalFact in additionalFacts) {
            val j = i
            addMessageWithFactDetail({ "$j. " }, additionalFact, messageType, level = originalLogLevel + 1)
            i++
        }
        addMessage({ "maxTransformationWeight: '$maxTransformationWeight'" }, messageType, originalLogLevel + 1)
        addMessage({ "maxBustCount: '$maxBustCount'" }, messageType, originalLogLevel + 1)
        addMessage({ "compareExpressionsWithProbabilityTest: '$minPossibleTransformationWeight'" }, messageType, originalLogLevel + 1)
        addMessage({ "additionalFactsSortedIdentifiers:" }, messageType, originalLogLevel + 1)
        add(additionalFactUsed.joinToString { if (it) "1" else "0"}, { "additionalFactUsed: '" }, {"'"}, messageType = messageType, level = originalLogLevel + 1)
        currentLevel = originalLogLevel
    }

    fun logFactsCompareAsIsParams (left: MainChainPart, right: MainChainPart, additionalFactsSortedIdentifiers: List<String>,
                                   compareExpressionsWithProbabilityTest: Boolean? = null, messageType: MessageType = MessageType.TECHNICAL){
        val originalLogLevel = currentLevel
        addMessageWithFactDetail({"Left fact: "}, left, messageType, originalLogLevel + 1)
        addMessageWithFactDetail({"Right fact: "}, right, messageType, originalLogLevel + 1)
        if (compareExpressionsWithProbabilityTest != null) {
            addMessage({ "compareExpressionsWithProbabilityTest: '$compareExpressionsWithProbabilityTest'" }, messageType, originalLogLevel + 1)
        }
        addMessage({ "additionalFactsSortedIdentifiers:" }, messageType, originalLogLevel + 1)
        var i = 0
        for (additionalFactsSortedIdentifier in additionalFactsSortedIdentifiers) {
            val j = i
            addMessage({ "$j. '''$additionalFactsSortedIdentifiers'''" }, messageType, level = originalLogLevel + 2)
            i++
        }
        currentLevel = originalLogLevel
    }
}

val log = LazyLog()