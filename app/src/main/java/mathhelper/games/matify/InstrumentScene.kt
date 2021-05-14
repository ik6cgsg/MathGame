package mathhelper.games.matify

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import api.expressionToStructureString
import expressiontree.ExpressionNode
import mathhelper.games.matify.activities.PlayActivity
import mathhelper.games.matify.common.AndroidUtil
import mathhelper.games.matify.common.ColorName
import mathhelper.games.matify.common.SimpleMathView
import mathhelper.games.matify.common.ThemeController
import org.w3c.dom.Text

enum class InstrumentType {
    OPPO, VAR, BRACKET, PERMUTE, MULTI
}

enum class InstrumentStep {
    DETAIL, PLACE, PARAM
}

data class InstrumentInfo(
    val type: InstrumentType,
    val detailRequired: Boolean,
    val placeRequired: Boolean,
    val paramRequired: Boolean,
    var button: Button,
    var isProcessing: Boolean = false,
    var detail: String? = null,
    var place: MutableList<ExpressionNode>? = null,
    var param: ExpressionNode? = null,
)

class StepInfo(
    val msg: TextView,
    val views: List<View>,
    val context: Context
) {
    fun set(required: Boolean, onClick: (() -> Unit)? = null) {
        if (required) {
            if (onClick == null) return
            setRequired(onClick)
        } else setDisabled()
    }

    fun setRequired(onClick: () -> Unit) {
        msg.isEnabled = true
        msg.isSelected = false
        msg.isClickable = true
        msg.setOnClickListener {
            onClick()
            toggle()
        }
        setRequiredStar()
        for (v in views) {
            v.visibility = View.GONE
        }
    }

    fun setDisabled() {
        msg.setCompoundDrawables(null, null, null, null)
        msg.isEnabled = false
        msg.isClickable = false
        for (v in views) {
            v.visibility = View.GONE
        }
    }

    fun setRequiredStar() {
        val d = context.getDrawable(R.drawable.required_star)
        d!!.setBounds(0, 0, 32, 32)
        msg.setCompoundDrawables(null, null, d, null)
    }

    fun setPassed() {
        val d = context.getDrawable(R.drawable.green_tick)
        d!!.setBounds(0, 0, 50, 50)
        msg.setCompoundDrawables(null, null, d, null)
    }

    fun toggle() {
        msg.isSelected = !msg.isSelected
        for (v in views) {
            AndroidUtil.toggleVisibility(v)
        }
    }
}

class InstrumentScene {
    companion object {
        val shared = InstrumentScene()
    }

    private lateinit var multiInstrument: InstrumentInfo
    private lateinit var oppoInstrument: InstrumentInfo
    private lateinit var varInstrument: InstrumentInfo
    private lateinit var bracketInstrument: InstrumentInfo
    private lateinit var permuteInstrument: InstrumentInfo
    private lateinit var instruments: HashMap<String, InstrumentInfo>
    private lateinit var steps: HashMap<InstrumentStep, StepInfo>
    // Global view
    private lateinit var instrumentHandleView: View
    // Current
    var currentProcessingInstrument: InstrumentInfo? = null
    private var currentStep: InstrumentStep? = null
    private var currentDetail: Button? = null
    private var currentEnteredText = ""
    // MathViews
    private lateinit var placeView: SimpleMathView
    private lateinit var paramView: SimpleMathView


    fun init(bottomSheet: View, activity: PlayActivity) {
        currentProcessingInstrument = null
        currentStep = null
        // Views
        val startStopMultiselectionMode = bottomSheet.findViewById<Button>(R.id.multi_inst)
        val oppoInstrumentView = bottomSheet.findViewById<Button>(R.id.oppo_inst)
        val varInstrumentView = bottomSheet.findViewById<Button>(R.id.var_inst)
        val bracketInstrumentView = bottomSheet.findViewById<Button>(R.id.bracket_inst)
        val permuteInstrumentView = bottomSheet.findViewById<Button>(R.id.permute_inst)
        instrumentHandleView = bottomSheet.findViewById(R.id.instrument_handle)
        instrumentHandleView.visibility = View.GONE
        placeView = bottomSheet.findViewById(R.id.inst_step_place)
        placeView.text = "expression"
        paramView = bottomSheet.findViewById(R.id.inst_step_param)
        paramView.text = "expression"
        // Step
        val stepDetailText = bottomSheet.findViewById<TextView>(R.id.inst_step_detail_msg)
        val stepDetailView = bottomSheet.findViewById<View>(R.id.inst_step_detail)
        val stepPlaceText = bottomSheet.findViewById<TextView>(R.id.inst_step_place_msg)
        val stepPlaceView = bottomSheet.findViewById<View>(R.id.inst_step_place_view)
        val stepParamText = bottomSheet.findViewById<TextView>(R.id.inst_step_param_msg)
        val stepParamView = bottomSheet.findViewById<View>(R.id.inst_step_param_view)
        val stepParamKeyboard = bottomSheet.findViewById<View>(R.id.inst_step_param_keyboard)
        steps = hashMapOf(
            InstrumentStep.DETAIL to StepInfo(stepDetailText, listOf(stepDetailView), activity),
            InstrumentStep.PLACE to StepInfo(stepPlaceText, listOf(stepPlaceView), activity),
            InstrumentStep.PARAM to StepInfo(stepParamText, listOf(stepParamView, stepParamKeyboard), activity)
        )
        val apply = bottomSheet.findViewById<Button>(R.id.apply)
        apply.setOnClickListener { apply(activity) }
        // LongClick
        startStopMultiselectionMode.setOnLongClickListener {
            activity.showMessage(
                activity.getString(R.string.end_multiselect_info),
                activity.globalMathView.multiselectionMode,
                activity.getString(R.string.start_multiselect_info)
            )
            true
        }
        oppoInstrumentView.setOnLongClickListener {
            activity.showMessage("Apply operator and inverse operator")
            true
        }
        varInstrumentView.setOnLongClickListener {
            activity.showMessage("Introduce a new variable ")
            true
        }
        bracketInstrumentView.setOnLongClickListener {
            activity.showMessage("Group expression nodes")
            true
        }
        permuteInstrumentView.setOnLongClickListener {
            activity.showMessage("Permute expression nodes")
            true
        }
        // Instruments
        multiInstrument = InstrumentInfo(InstrumentType.MULTI,
            detailRequired = false, placeRequired = false, paramRequired = false,
            button = startStopMultiselectionMode
        )
        oppoInstrument = InstrumentInfo(InstrumentType.OPPO,
            detailRequired = true, placeRequired = true, paramRequired = true,
            button = oppoInstrumentView
        )
        varInstrument = InstrumentInfo(InstrumentType.VAR,
            detailRequired = false, placeRequired = true, paramRequired = true,
            button = varInstrumentView
        )
        bracketInstrument = InstrumentInfo(InstrumentType.BRACKET,
            detailRequired = false, placeRequired = true, paramRequired = false,
            button = bracketInstrumentView
        )
        permuteInstrument = InstrumentInfo(InstrumentType.PERMUTE,
            detailRequired = false, placeRequired = true, paramRequired = true,
            button = permuteInstrumentView
        )
        instruments = hashMapOf(
            InstrumentType.MULTI.name to multiInstrument,
            InstrumentType.OPPO.name to oppoInstrument,
            InstrumentType.VAR.name to varInstrument,
            InstrumentType.BRACKET.name to bracketInstrument,
            InstrumentType.PERMUTE.name to permuteInstrument,
        )
    }

    fun clickInstrument(tag: String, context: Context) {
        val inst = instruments[tag.toUpperCase()] ?: return
        if (inst.type == InstrumentType.MULTI) {
            PlayScene.shared.playActivity?.globalMathView?.multiselectionMode = !(PlayScene.shared.playActivity?.globalMathView?.multiselectionMode ?: false)
        }
        if (inst.isProcessing) turnOffInstrument(inst, context) else turnOnInstrument(inst, context)
    }

    fun clickDetail(v: Button) {
        AndroidUtil.vibrateLight(PlayScene.shared.playActivity!!)
        if (currentStep != InstrumentStep.DETAIL || currentDetail == v) return
        if (currentDetail != null) {
            currentDetail?.isSelected = false
        }
        v.isSelected = true
        currentDetail = v
        currentProcessingInstrument?.detail = v.tag.toString()
        steps[InstrumentStep.DETAIL]?.setPassed()
    }

    fun clickKeyboard(v: Button) {
        AndroidUtil.vibrateLight(PlayScene.shared.playActivity!!)
        if (currentStep != InstrumentStep.PARAM || currentProcessingInstrument?.type == InstrumentType.PERMUTE) return
        if (v.tag is String && v.tag.toString() == "delete") {
            currentEnteredText = ""
        } else {
            currentEnteredText += v.text
        }
        paramView.setExpression(currentEnteredText, null)
        if (paramView.text == "parsing error" || paramView.text == "") {
            currentProcessingInstrument?.param = null
            steps[InstrumentStep.PARAM]?.setRequiredStar()
        } else {
            currentProcessingInstrument?.param = paramView.expression
            steps[InstrumentStep.PARAM]?.setPassed()
        }
    }

    fun choosenAtom(nodes: MutableList<ExpressionNode>) {
        when (currentStep) {
            InstrumentStep.PLACE -> {
                if (currentProcessingInstrument?.type == InstrumentType.BRACKET) {
                    placeView.text = if (nodes.isEmpty()) "" else PlayScene.shared.playActivity!!.globalMathView.text
                } else {
                    placeView.setExpression(expressionToStructureString(nodes[0]), null)
                }
                if (placeView.text.toString() == "parsing error" || placeView.text.toString() == "") {
                    currentProcessingInstrument?.place = null
                    steps[InstrumentStep.PLACE]?.setRequiredStar()
                } else {
                    currentProcessingInstrument?.place = nodes
                    steps[InstrumentStep.PLACE]?.setPassed()
                }
            }
            InstrumentStep.PARAM -> {
                currentEnteredText = ""
                paramView.setExpression(nodes[0].parent ?: return, null)
                steps[InstrumentStep.PARAM]?.setPassed()
                if (paramView.text == "parsing error" || paramView.text == "") {
                    steps[InstrumentStep.PLACE]?.setRequiredStar()
                    currentProcessingInstrument?.param = null
                } else {
                    currentProcessingInstrument?.param = nodes[0]
                    steps[InstrumentStep.PLACE]?.setPassed()
                }
            }
            else -> return
        }
    }

    fun turnOnInstrument(inst: InstrumentInfo, context: Context) {
        var needMoveBottom = true
        if (currentProcessingInstrument == inst) return
        else {
            if (currentProcessingInstrument != null) needMoveBottom = false
            turnOffInstrument(currentProcessingInstrument, context, needMoveBottom)
        }
        inst.isProcessing = true
        currentProcessingInstrument = inst
        currentStep = null
        currentDetail?.isSelected = false
        currentDetail = null
        currentEnteredText = ""
        PlayScene.shared.instrumetProcessing = true
        //startStopMultiselectionMode.text = getText(R.string.end_multiselect)
        inst.button.setTextColor(Color.RED)
        PlayScene.shared.playActivity?.showMessage("Enter instrument processing")
        AndroidUtil.vibrate(context)
        PlayScene.shared.playActivity?.mainViewAnim?.startTransition(300)
        if (inst.type == InstrumentType.MULTI) {
            PlayScene.shared.playActivity?.globalMathView?.recolorCurrentAtom(ThemeController.shared.getColor(context, ColorName.MULTISELECTION_COLOR))
        } else {
            PlayScene.shared.playActivity?.globalMathView?.clearExpression()
            PlayScene.shared.playActivity?.globalMathView?.multiselectionMode = false
        }
        if (inst.type == InstrumentType.BRACKET) {
            PlayScene.shared.playActivity?.globalMathView?.multiselectionMode = true
        }
        setInstrumentHandleView(inst, context)
    }

    fun turnOffInstrument(inst: InstrumentInfo?, context: Context, collapse: Boolean = true) {
        if (inst == null) return
        instrumentHandleView.visibility = View.GONE
        inst.isProcessing = false
        currentProcessingInstrument = null
        PlayScene.shared.instrumetProcessing = false
        //but.text = getText(R.string.start_multiselect)
        inst.button.setTextColor(ThemeController.shared.getColor(context, ColorName.PRIMARY_COLOR))
        PlayScene.shared.playActivity?.globalMathView?.clearExpression()
        PlayScene.shared.clearRules()
        AndroidUtil.vibrate(context)
        PlayScene.shared.playActivity?.mainViewAnim?.reverseTransition(300)
        if (collapse) {
            PlayScene.shared.playActivity?.collapseBottomSheet()
        }
    }

    fun turnOffCurrentInstrument(context: Context) {
        turnOffInstrument(currentProcessingInstrument, context)
    }

    fun setInstrumentHandleView(inst: InstrumentInfo, context: Context, expand: Boolean = true) {
        if (inst.type == InstrumentType.MULTI) return
        instrumentHandleView.visibility = View.VISIBLE
        steps[InstrumentStep.DETAIL]?.set(inst.detailRequired) {
            AndroidUtil.vibrateLight(context)
            activateStep(InstrumentStep.DETAIL)
        }
        steps[InstrumentStep.PLACE]?.set(inst.placeRequired) {
            AndroidUtil.vibrateLight(context)
            activateStep(InstrumentStep.PLACE)
        }
        steps[InstrumentStep.PARAM]?.set(inst.paramRequired) {
            AndroidUtil.vibrateLight(context)
            activateStep(InstrumentStep.PARAM)
        }
        activateStep(InstrumentStep.PLACE)
        steps[InstrumentStep.PLACE]?.toggle()
        placeView.text = "expression"
        paramView.text = "expression"
        if (expand) {
            PlayScene.shared.playActivity?.halfExpandBottomSheet()
        }
    }

    fun apply(context: Context) {
        if (currentProcessingInstrument == null) return
        if (currentProcessingInstrument!!.detailRequired && currentProcessingInstrument!!.detail == null ||
            currentProcessingInstrument!!.placeRequired && currentProcessingInstrument!!.place == null ||
            currentProcessingInstrument!!.paramRequired && currentProcessingInstrument!!.param == null
        ) {
            Toast.makeText(context, "Fill required fields!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Ready to apply!!!", Toast.LENGTH_SHORT).show()
        }
    }

    fun activateStep(newStep: InstrumentStep?) {
        currentStep = when (currentStep) {
            newStep -> null
            null -> newStep
            else -> {
                steps[currentStep]?.toggle()
                newStep
            }
        }
    }
}