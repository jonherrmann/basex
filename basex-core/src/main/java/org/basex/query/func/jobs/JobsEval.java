package org.basex.query.func.jobs;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class JobsEval extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    checkAdmin(qc);

    final String query = string(toToken(exprs[0], qc));
    final HashMap<String, Value> bindings = toBindings(1, qc);

    final EvalOptions opts = new EvalOptions();
    if(exprs.length > 2) toOptions(2, opts, qc);

    // copy variable values
    final Context ctx = qc.context;
    for(final Entry<String, Value> it : bindings.entrySet()) {
      final String key = it.getKey();
      bindings.put(key, ScheduledXQuery.copy(it.getValue().iter(), ctx, qc));
    }

    // check if number of maximum queries has been reached
    if(ctx.jobs.active.size() >= JobPool.MAXQUERIES) throw JOBS_OVERFLOW.get(info);

    final ScheduledXQuery job = new ScheduledXQuery(query, bindings, opts, info, qc, sc);
    return Str.get(job.jc().id());
  }
}
