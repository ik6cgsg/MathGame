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
    val view: View,
    val keyboard: View? = null,
    val context: Context
) {
    private var needKey: Boolean = true

    fun set(required: Boolean, needKey: Boolean = true, onClick: (() -> Unit)? = null) {
        this.needKey = needKey
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
        view.visibility = View.GONE
        keyboard?.visibility = View.GONE
    }

    fun setDisabled() {
        msg.setCompoundDrawables(null, null, null, null)
        msg.isEnabled = false
        msg.isClickable = false
        view.visibility = View.GONE
        keyboard?.visibility = View.GONE
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
        AndroidUtil.toggleVisibility(view)
        if (needKey && keyboard != null) {
            AndroidUtil.toggleVisibility(keyboard)
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
        placeView.text = ""
        paramView = bottomSheet.findViewById(R.id.inst_step_param)
        paramView.text = ""
        // Step
        val stepDetailText = bottomSheet.findViewById<TextView>(R.id.inst_step_detail_msg)
        val stepDetailView = bottomSheet.findViewById<View>(R.id.inst_step_detail)
        val stepPlaceText = bottomSheet.findViewById<TextView>(R.id.inst_step_place_msg)
        val stepPlaceView = bottomSheet.findViewById<View>(R.id.inst_step_place_view)
        val stepParamText = bottomSheet.findViewById<TextView>(R.id.inst_step_param_msg)
        val stepParamView = bottomSheet.findViewById<View>(R.id.inst_step_param_view)
        val stepParamKeyboard = bottomSheet.findViewById<View>(R.id.inst_step_param_keyboard)
        steps = hashMapOf(
            InstrumentStep.DETAIL to StepInfo(stepDetailText, stepDetailView, context = activity),
            InstrumentStep.PLACE to StepInfo(stepPlaceText, stepPlaceView, context = activity),
            InstrumentStep.PARAM to StepInfo(stepParamText, stepParamView, stepParamKeyboard, activity)
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
            activity.showMessage(activity.getString(R.string.oppo_descr))
            true
        }
        varInstrumentView.setOnLongClickListener {
            activity.showMessage(activity.getString(R.string.var_descr))
            true
        }
        bracketInstrumentView.setOnLongClickListener {
            activity.showMessage(activity.getString(R.string.bracket_descr))
            true
        }
        permuteInstrumentView.setOnLongClickListener {
            activity.showMessage(activity.getString(R.string.permute_descr))
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
        if (currentProcessingInstrument == inst) return
        else {
            turnOffInstrument(currentProcessingInstrument, context, false)
        }
        inst.isProcessing = true
        currentProcessingInstrument = inst
        currentStep = null
        currentDetail?.isSelected = false
        currentDetail = null
        currentEnteredText = ""
        PlayScene.shared.instrumetProcessing = true
        PlayScene.shared.clearRules()
        //startStopMultiselectionMode.text = getText(R.string.end_multiselect)
        if (inst.type == InstrumentType.MULTI || inst.type == InstrumentType.BRACKET) {
            PlayScene.shared.setMultiselectionMode(true)
        } else {
            PlayScene.shared.setMultiselectionMode(false)
        }
        inst.button.setTextColor(Color.RED)
        PlayScene.shared.playActivity?.showMessage(context.getString(R.string.inst_enter))
        PlayScene.shared.playActivity?.rulesMsg?.text = context.getString(R.string.inst_rules_msg)
        AndroidUtil.vibrate(context)
        PlayScene.shared.playActivity?.mainViewAnim?.startTransition(300)
        setInstrumentHandleView(inst, context)
    }

    fun turnOffInstrument(inst: InstrumentInfo?, context: Context, collapse: Boolean = true) {
        if (inst == null) return
        instrumentHandleView.visibility = View.GONE
        inst.isProcessing = false
        currentProcessingInstrument = null
        inst.button.setTextColor(ThemeController.shared.color(ColorName.PRIMARY_COLOR))
        PlayScene.shared.setMultiselectionMode(false)
        PlayScene.shared.instrumetProcessing = false
        PlayScene.shared.playActivity?.globalMathView?.clearExpression()
        AndroidUtil.vibrate(context)
        PlayScene.shared.playActivity?.mainViewAnim?.reverseTransition(300)
        if (collapse) {
            PlayScene.shared.playActivity?.collapseBottomSheet()
            PlayScene.shared.clearRules()
        }
    }

    fun turnOffCurrentInstrument(context: Context) {
        turnOffInstrument(currentProcessingInstrument, context)
    }

    fun setInstrumentHandleView(inst: InstrumentInfo, context: Context) {
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
        val keyNeed = inst.type != InstrumentType.PERMUTE
        steps[InstrumentStep.PARAM]?.set(inst.paramRequired, keyNeed) {
            AndroidUtil.vibrateLight(context)
            activateStep(InstrumentStep.PARAM)
        }
        activateStep(InstrumentStep.PLACE)
        steps[InstrumentStep.PLACE]?.toggle()
        placeView.text = ""
        paramView.text = ""
        PlayScene.shared.playActivity?.halfExpandBottomSheet()
    }

    fun apply(context: Context) {
        if (currentProcessingInstrument == null) return
        if (currentProcessingInstrument!!.detailRequired && currentProcessingInstrument!!.detail == null ||
            currentProcessingInstrument!!.placeRequired && currentProcessingInstrument!!.place == null ||
            currentProcessingInstrument!!.paramRequired && currentProcessingInstrument!!.param == null
        ) {
            Toast.makeText(context, context.getString(R.string.inst_fill_req), Toast.LENGTH_SHORT).show()
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