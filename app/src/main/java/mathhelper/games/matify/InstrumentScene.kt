package mathhelper.games.matify

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import mathhelper.games.matify.common.*
import mathhelper.twf.api.expressionToStructureString
import mathhelper.twf.expressiontree.ExpressionNode
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

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
        val d = ContextCompat.getDrawable(context, R.drawable.required_star)
        d!!.setBounds(0, 0, 32, 32)
        AndroidUtil.setRightDrawable(msg, d)
    }

    fun setPassed() {
        val d = ContextCompat.getDrawable(context, R.drawable.green_tick)
        d!!.setBounds(0, 0, 50, 50)
        AndroidUtil.setRightDrawable(msg, d)
    }

    fun toggle() {
        msg.isSelected = !msg.isSelected
        AndroidUtil.toggleVisibility(view)
        if (needKey && keyboard != null) {
            AndroidUtil.toggleVisibility(keyboard)
        }
    }
}

interface InstrumentSceneListener {
    var globalMathView: GlobalMathView
    val ctx: Context
    fun getString(varDescr: Int): String
    fun startInstrumentProcessing(setMSMode: Boolean)
    fun endInstrumentProcessing(collapse: Boolean)

    fun showMessage(varDescr: Int)
    fun setMultiselectionMode(multi: Boolean)
    fun halfExpandBottomSheet()
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
    private var instrumentHandleViewRef: WeakReference<View> = WeakReference(null)

    // Current
    var currentProcessingInstrument: InstrumentInfo? = null
    private var currentStep: InstrumentStep? = null
    private var currentDetailRef: WeakReference<Button> = WeakReference(null)
    private var currentEnteredText = ""

    // MathViews
    private lateinit var placeView: SimpleMathView
    private lateinit var paramView: SimpleMathView
    private lateinit var listenerRef: WeakReference<InstrumentSceneListener>


    fun init(bottomSheet: View, listener: InstrumentSceneListener) {
        listenerRef = WeakReference(listener)
        currentProcessingInstrument = null
        currentStep = null
        // Views
        val startStopMultiselectionMode = bottomSheet.findViewById<Button>(R.id.multi_inst)
        val oppoInstrumentView = bottomSheet.findViewById<Button>(R.id.oppo_inst)
        val varInstrumentView = bottomSheet.findViewById<Button>(R.id.var_inst)
        val bracketInstrumentView = bottomSheet.findViewById<Button>(R.id.bracket_inst)
        val permuteInstrumentView = bottomSheet.findViewById<Button>(R.id.permute_inst)
        val instHView: View = bottomSheet.findViewById(R.id.instrument_handle)
        instrumentHandleViewRef = WeakReference(instHView)
        instHView.visibility = View.GONE
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
            InstrumentStep.DETAIL to StepInfo(stepDetailText, stepDetailView, context = listener.ctx),
            InstrumentStep.PLACE to StepInfo(stepPlaceText, stepPlaceView, context = listener.ctx),
            InstrumentStep.PARAM to StepInfo(stepParamText, stepParamView, stepParamKeyboard, listener.ctx)
        )
        val apply = bottomSheet.findViewById<Button>(R.id.apply)
        apply.setOnClickListener { apply(listener.ctx) }
        // LongClick
        startStopMultiselectionMode.setOnLongClickListener {
            listener.showMessage(
                if (listener.globalMathView.multiselectionMode)
                    R.string.end_multiselect_info
                else
                    R.string.start_multiselect_info
            )
            true
        }
        oppoInstrumentView.setOnLongClickListener {
            listener.showMessage(R.string.oppo_descr)
            true
        }
        varInstrumentView.setOnLongClickListener {
            listener.showMessage(R.string.var_descr)
            true
        }
        bracketInstrumentView.setOnLongClickListener {
            listener.showMessage(R.string.bracket_descr)
            true
        }
        permuteInstrumentView.setOnLongClickListener {
            listener.showMessage(R.string.permute_descr)
            true
        }
        // Instruments
        multiInstrument = InstrumentInfo(
            InstrumentType.MULTI,
            detailRequired = false, placeRequired = false, paramRequired = false,
            button = startStopMultiselectionMode
        )
        oppoInstrument = InstrumentInfo(
            InstrumentType.OPPO,
            detailRequired = true, placeRequired = true, paramRequired = true,
            button = oppoInstrumentView
        )
        varInstrument = InstrumentInfo(
            InstrumentType.VAR,
            detailRequired = false, placeRequired = true, paramRequired = true,
            button = varInstrumentView
        )
        bracketInstrument = InstrumentInfo(
            InstrumentType.BRACKET,
            detailRequired = false, placeRequired = true, paramRequired = false,
            button = bracketInstrumentView
        )
        permuteInstrument = InstrumentInfo(
            InstrumentType.PERMUTE,
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

    fun clickInstrument(tag: String) {
        val inst = instruments[tag.toUpperCase(Locale.ROOT)] ?: return
        if (inst.isProcessing) turnOffInstrument(inst) else turnOnInstrument(inst)
    }

    fun clickDetail(v: Button) {
        listenerRef.get()?.let { AndroidUtil.vibrateLight(it.ctx) }
        val currentDetail = currentDetailRef.get()
        if (currentStep != InstrumentStep.DETAIL || currentDetail == v) return
        currentDetail?.isSelected = false
        v.isSelected = true
        currentDetailRef = WeakReference(v)
        currentProcessingInstrument?.detail = v.tag.toString()
        steps[InstrumentStep.DETAIL]?.setPassed()
    }

    fun clickKeyboard(v: Button) {
        listenerRef.get()?.let { AndroidUtil.vibrateLight(it.ctx) }
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

    fun choosenAtom(nodes: MutableList<ExpressionNode>, mathViewText: CharSequence) {
        when (currentStep) {
            InstrumentStep.PLACE -> {
                if (currentProcessingInstrument?.type == InstrumentType.BRACKET) {
                    placeView.text = if (nodes.isEmpty()) "" else mathViewText
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

    fun turnOnInstrument(inst: InstrumentInfo) {
        if (currentProcessingInstrument == inst) return
        else {
            turnOffInstrument(currentProcessingInstrument, false)
        }
        inst.isProcessing = true
        currentProcessingInstrument = inst
        currentStep = null
        currentDetailRef.get()?.isSelected = false
        currentDetailRef.clear()
        currentEnteredText = ""

        inst.button.setTextColor(Color.RED)
        val activity = listenerRef.get() ?: return
        activity.startInstrumentProcessing(inst.type == InstrumentType.MULTI || inst.type == InstrumentType.BRACKET)
        setInstrumentHandleView(inst, activity.ctx)
    }

    fun turnOffInstrument(inst: InstrumentInfo?, collapse: Boolean = true) {
        if (inst == null) return
        instrumentHandleViewRef.get()?.visibility = View.GONE
        inst.isProcessing = false
        currentProcessingInstrument = null
        inst.button.setTextColor(ThemeController.shared.color(ColorName.PRIMARY_COLOR))
        listenerRef.get()?.endInstrumentProcessing(collapse)
    }

    fun turnOffCurrentInstrument() {
        turnOffInstrument(currentProcessingInstrument)
    }

    fun setInstrumentHandleView(inst: InstrumentInfo, context: Context) {
        if (inst.type == InstrumentType.MULTI) return
        instrumentHandleViewRef.get()?.visibility = View.VISIBLE
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
        listenerRef.get()?.halfExpandBottomSheet()
    }

    fun apply(context: Context) {
        currentProcessingInstrument?.let {
            if (it.detailRequired && it.detail == null ||
                it.placeRequired && it.place == null ||
                it.paramRequired && it.param == null
            ) {
                Toast.makeText(context, context.getString(R.string.inst_fill_req), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Ready to apply!!!", Toast.LENGTH_SHORT).show()
            }
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