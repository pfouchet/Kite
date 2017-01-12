/**
 * Main components :
 * <p>
 * Provided by users using Json :
 * {@link com.groupeseb.kite.Command}: HTTP Request definition.
 * {@link com.groupeseb.kite.Scenario} : aet of Command, usually contained in a file, it corresponds to a test suite.
 * <p>
 * Internal classes :
 * <p>
 * {@link com.groupeseb.kite.ScenarioRunner} : execute the {@link com.groupeseb.kite.Scenario}.
 * {@link com.groupeseb.kite.CommandRunner} : execute the {@link com.groupeseb.kite.Command}, replace all placeholders and verify checks.
 * {@link com.groupeseb.kite.function.Function} :
 * {@link com.groupeseb.kite.check.Check}
 * <p>
 * <B>Note:</B> Non-primitive parameters, fields, and method return values in this package are '<TT>@Nonnull</TT>' by
 * default unless there is an explicit '<TT>@Nullable</TT>' annotation.
 */
@ParametersAreNonnullByDefault
package com.groupeseb.kite;

import javax.annotation.ParametersAreNonnullByDefault;