/**
 * Copyright 2014 Lennart Koopmann <lennart@torch.sh>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package models.dashboards.widgets;

import com.google.common.collect.Maps;
import lib.timeranges.TimeRange;
import models.dashboards.Dashboard;
import play.mvc.Call;

import java.util.Map;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class SearchResultChartWidget extends DashboardWidget {

    private static final int WIDTH = 2;
    private static final int HEIGHT = 1;

    private final String query;
    private final TimeRange timerange;
    private final String streamId;
    private final String interval;

    public SearchResultChartWidget(Dashboard dashboard, String query, TimeRange timerange, String description, String streamId, String interval) {
        this(dashboard, null, description, streamId, 0, query, timerange, null, interval);
    }

    public SearchResultChartWidget(Dashboard dashboard, String id, String description, String streamId, int cacheTime, String query, TimeRange timerange, String creatorUserId, String interval) {
        super(Type.SEARCH_RESULT_CHART, id, description, cacheTime, dashboard, creatorUserId);

        this.query = query;
        this.timerange = timerange;
        this.streamId = streamId;
        this.interval = interval;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = Maps.newHashMap();
        config.putAll(timerange.getQueryParams());
        config.put("query", query);
        config.put("stream_id", streamId);
        config.put("interval", interval);

        return config;
    }

    @Override
    public Call replayRoute() {
        if (streamId == null || streamId.isEmpty()) {
            return prepareNonStreamBoundReplayRoute(query, timerange);
        } else {
            return prepareStreamBoundReplayRoute(streamId, query, timerange);
        }
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

}