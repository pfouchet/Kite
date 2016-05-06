package com.groupeseb.kite.check;

import com.google.common.base.Preconditions;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class DefaultCheckRunner {
	private final Collection<ICheckOperator> checkOperators;
	private final Collection<ICheckMethod> checkMethods;

	@Autowired
	DefaultCheckRunner(Collection<ICheckOperator> checkOperators, Collection<ICheckMethod> checkMethods) {
		this.checkOperators = checkOperators;
		this.checkMethods = checkMethods;
	}

	private ICheckOperator getMatchingOperator(String operatorName) {
		ICheckOperator match = null;
		Integer matchCount = 0;

		for (ICheckOperator operator : checkOperators) {
			if (operator.match(operatorName)) {
				match = operator;
				matchCount++;
			}

			if (matchCount > 1) {
				throw new UnsupportedOperationException("Several (" + matchCount + ") operators match but only one is allowed.");
			}
		}

		if (matchCount == 0) {
			throw new IndexOutOfBoundsException("No matching operator found for '" + operatorName + "'");
		}

		return match;
	}

	private ICheckMethod getMatchingMethod(String methodName) {
		ICheckMethod match = null;
		Integer matchCount = 0;

		for (ICheckMethod operator : checkMethods) {
			if (operator.match(methodName)) {
				match = operator;
				matchCount++;
			}

			if (matchCount > 1) {
				throw new UnsupportedOperationException("Several (" + matchCount + ") operators match but only one match is allowed.");
			}
		}

		if (matchCount == 0) {
			throw new IndexOutOfBoundsException("No matching method found for '" + methodName + "'");
		}

		return match;
	}

	public void verify(Check check, String responseBody) throws ParseException {
		log.info("Checking " + check.getDescription() + "...");

		if (check.getSkip()) {
			log.warn("Check skipped (" + check.getDescription() + ")");
			return;
		}

		ICheckOperator operator = getMatchingOperator(check.getOperatorName());
		ICheckMethod method = getMatchingMethod(check.getMethodName());

		Object node = JsonPath.read(responseBody, check.getFieldName());
		if (check.getForeach()) {
			Preconditions.checkArgument(node instanceof Iterable, "Using 'forEach' mode for check requires an iterable node.");

			@SuppressWarnings({"unchecked", "ConstantConditions"})
			Iterable nodeList = (Iterable) node;

			if (check.getMustMatch()) {
				Preconditions.checkArgument(nodeList.iterator().hasNext(), check.getDescription() + " (No match found but 'mustMatch' was set to true)");
			}

			for (Object o : nodeList) {
				operator.apply(method.apply(o, check.getParameters()),
						parseExpectedValue(check.getExpectedValue(), responseBody),
						check.getDescription(), check.getParameters());
			}
		} else {
			operator.apply(method.apply(node, check.getParameters()),
					parseExpectedValue(check.getExpectedValue(), responseBody),
					check.getDescription(), check.getParameters());
		}
	}

	private Object parseExpectedValue(Object expectedValue, String responseBody) {
//        if (String.class.isAssignableFrom(expectedValue.getClass())) {
//            Pattern lookupPattern = Pattern.compile("\\{\\{Lookup\\:%\\.(.+)\\}\\}");
//            Matcher matcher = lookupPattern.matcher(expectedValue.toString());
//
//            if (matcher.find()) {
//                String path = matcher.group(1);
//                return JsonPath.read(responseBody, path);
//            }
//        }

		return expectedValue;
	}
}
