/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.o19s.es.termstat;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.search.Query;
import org.opensearch.core.common.ParsingException;
import org.opensearch.index.query.QueryShardContext;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.AbstractQueryTestCase;
import org.opensearch.test.TestGeoShapeFieldMapperPlugin;

import com.o19s.es.explore.StatisticsHelper.AggrType;
import com.o19s.es.ltr.LtrQueryParserPlugin;

public class TermStatQueryBuilderTests extends AbstractQueryTestCase<TermStatQueryBuilder> {
    // TODO: Remove the TestGeoShapeFieldMapperPlugin once upstream has completed the migration.
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return asList(LtrQueryParserPlugin.class, TestGeoShapeFieldMapperPlugin.class);
    }

    @Override
    protected TermStatQueryBuilder doCreateTestQueryBuilder() {
        TermStatQueryBuilder builder = new TermStatQueryBuilder();

        builder.analyzer("standard");
        builder.expr("tf");
        builder.aggr(AggrType.AVG.getType());
        builder.posAggr(AggrType.AVG.getType());
        builder.fields(new String[] { "text" });
        builder.terms(new String[] { "cow" });

        return builder;
    }

    public void testParse() throws Exception {
        String query = " {"
            + "  \"term_stat\": {"
            + "   \"expr\": \"tf\","
            + "   \"aggr\": \"min\","
            + "   \"pos_aggr\": \"max\","
            + "   \"fields\": [\"text\"],"
            + "   \"terms\":  [\"cow\"]"
            + "  }"
            + "}";

        TermStatQueryBuilder builder = (TermStatQueryBuilder) parseQuery(query);

        assertEquals(builder.expr(), "tf");
        assertEquals(builder.aggr(), "min");
        assertEquals(builder.posAggr(), "max");

    }

    public void testMissingExpr() throws Exception {
        String query = " {"
            + "  \"term_stat\": {"
            + "   \"aggr\": \"min\","
            + "   \"pos_aggr\": \"max\","
            + "   \"fields\": [\"text\"],"
            + "   \"terms\": [\"cow\"]"
            + "  }"
            + "}";

        expectThrows(ParsingException.class, () -> parseQuery(query));
    }

    @Override
    protected void doAssertLuceneQuery(TermStatQueryBuilder queryBuilder, Query query, QueryShardContext context) throws IOException {
        assertThat(query, instanceOf(TermStatQuery.class));
    }
}
