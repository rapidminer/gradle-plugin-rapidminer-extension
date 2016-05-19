package $PACKAGE_NAME$;

import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;


public class $OPERATOR_NAME$ extends Operator {

	// private InputPort inputPort;
	// private OutputPort outputPort;

	/**
	 * Default operator constructor.
	 *
	 * @param description
	 *            the opreator description
	 */
	public $OPERATOR_NAME$(OperatorDescription description) {
		super(description);

		// create input and output ports
		// inputPort = getInputPorts().createPort("example input");
		// outputPort = getOutputPorts().createPort("example output");

		// transform meta data
		// getTransformer().addRule(new MDTransformationRule() {
		//
		// @Override
		// public void transformMD() {
		// // transform meta data here
		// }
		// });

	}

	@Override
	public void doWork() throws OperatorException {

		// ExampleSet data = inputPort.getData(ExampleSet.class);
		// // implement operator logic here
		// outputPort.deliver(data);

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();

		// add parameter types here

		return parameterTypes;
	}

}
