/* Copyright (c) 2014, The Broad Institute
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of The Broad Institute nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL The Broad Institute BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bard.core.rest.spring

import bard.core.SearchParams
import bard.core.interfaces.RestApiConstants
import bard.core.rest.spring.compounds.CompoundResult
import bard.core.rest.spring.experiment.*
import bard.core.rest.spring.project.ProjectResult
import bard.core.rest.spring.util.MetaData
import bard.core.util.FilterTypes
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException

class ExperimentRestService extends AbstractRestService {
    def transactional = false

    public String getResourceContext() {
        return RestApiConstants.EXPERIMENTS_RESOURCE;
    }

    public List<ExperimentSearch> findRecentlyAdded(long top) {
        try {
            String recentURL = "${RestApiConstants.RECENT}${top}${RestApiConstants.QUESTION_MARK}expand=true"
            final String urlString = getResource(recentURL)
            final URL url = new URL(urlString)
            final List<ExperimentSearch> experiments = (List) getForObject(url.toURI(), List.class)
            return experiments
        } catch (Exception ee) {
            log.error(ee, ee)
            return []
        }
    }

    //Why is this returning a string and not a java object?
    //Everytime we do this it makes it harder for CACHE because we either
    //have to generate the same JSON or change the client sode code
    public String histogramDataByEID(final Long eid) {
        if (eid) {
            final String urlString = buildURLToExperimentHistogramData(eid)
            final URL url = new URL(urlString)
            final String histogramJson = (String) this.getForObject(url.toURI(), String.class)
            return histogramJson
        }
        return null

    }



    public ExperimentData activitiesByEIDs(final List<Long> eids, final SearchParams searchParams) {
        if (eids) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("eids", eids.join(","));
            final String urlString = buildURLToExperimentData(searchParams)
            final URL url = new URL(urlString)
            final List<Activity> activities = this.postForObject(url.toURI(), Activity[].class, map) as List<Activity>;
            ExperimentData experimentData = new ExperimentData()
            experimentData.setActivities(activities)
            return experimentData
        }
        return null

    }

    public ExperimentData activitiesByADIDs(final List<Long> adids, final SearchParams searchParams) {
        if (adids) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("aids", adids.join(","));
            final String urlString = buildURLToExperimentData(searchParams)
            final URL url = new URL(urlString)
            final List<Activity> activities = this.postForObject(url.toURI(), Activity[].class, map) as List<Activity>;
            ExperimentData experimentData = new ExperimentData()
            experimentData.setActivities(activities)
            return experimentData
        }
        return null
    }

    public ExperimentData activitiesBySIDs(final List<Long> sids, final SearchParams searchParams) {
        if (sids) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("sids", sids.join(","));
            final String urlString = buildURLToExperimentData(searchParams)
            final URL url = new URL(urlString)
            final List<Activity> activities = this.postForObject(url.toURI(), Activity[].class, map) as List<Activity>;
            ExperimentData experimentData = new ExperimentData()
            experimentData.setActivities(activities)
            return experimentData
        }
        return null
    }

    public ExperimentData activitiesByCIDs(final List<Long> cids, final SearchParams searchParams) {
        if (cids) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("cids", cids.join(","));
            final String urlString = buildURLToExperimentData(searchParams)
            final URL url = new URL(urlString)
            final List<Activity> activities = this.postForObject(url.toURI(), Activity[].class, map) as List<Activity>;
            ExperimentData experimentData = new ExperimentData()
            experimentData.setActivities(activities)
            return experimentData
        }
        return null
    }


    String buildURLToExperimentHistogramData(final Long eid) {
        final StringBuilder resource =
            new StringBuilder(this.externalUrlDTO.ncgcUrl).append(RestApiConstants.EXPERIMENTS_RESOURCE)

        resource.append(RestApiConstants.FORWARD_SLASH).append(eid).append(RestApiConstants.RESULT_TYPES)
                .append(RestApiConstants.QUESTION_MARK).append(RestApiConstants.EXPAND_TRUE).append(RestApiConstants.AMPERSAND)
                .append(RestApiConstants.COLLAPSE_RESULTS).append(RestApiConstants.MAXIMUM_NUMBER_OF_HISTOGRAM_BARS)
        return resource.toString()

    }


    String buildURLToExperimentData(final SearchParams searchParams) {
        final StringBuilder resource =
            new StringBuilder(this.externalUrlDTO.ncgcUrl).append(RestApiConstants.EXPTDATA_RESOURCE)

        if (searchParams.getTop()) {
            resource.append(RestApiConstants.QUESTION_MARK)
            resource.append(RestApiConstants.SKIP).
                    append(searchParams.getSkip()).
                    append(RestApiConstants.TOP).
                    append(searchParams.getTop())
        }
        return resource.toString()

    }




    String buildURLToExptDataByIds(final SearchParams searchParams) {
        final StringBuilder resource =
            new StringBuilder(this.externalUrlDTO.ncgcUrl).
                    append(RestApiConstants.EXPTDATA_RESOURCE).
                    append(RestApiConstants.EXPTDATABYIDS_RESOURCE)

        if (searchParams.getTop()) {
            resource.append(RestApiConstants.QUESTION_MARK)
            resource.append(RestApiConstants.SKIP).
                    append(searchParams.getSkip()).
                    append(RestApiConstants.TOP).
                    append(searchParams.getTop())
        }
        return resource.toString()

    }

    /**
     *
     * @param eid
     * @return {@link bard.core.rest.spring.experiment.ExperimentSearch}
     */
    public ExperimentShow getExperimentById(Long eid) {
        final String url = buildEntityURL() + "?expand={expand}"
        final Map map = [id: eid, expand: "true"]
        final ExperimentShow experimentShow = (ExperimentShow) this.getForObject(url, ExperimentShow.class, map)
        return experimentShow;
    }
    /**
     *
     * @param list of pids
     * @return {@link ExperimentSearchResult}
     */
    public ExperimentSearchResult searchExperimentsByIds(final List<Long> eids) {
        if (eids) {
            final Map<String, Long> etags = [:]
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("ids", eids.join(","));

            HttpHeaders headers = new HttpHeaders();
            this.addETagsToHTTPHeader(headers, etags)
            HttpEntity<List> entity = new HttpEntity<List>(map, headers);
            final String url = this.buildURLToPostIds()

            final HttpEntity<List> exchange = postExchange(url, entity, ExperimentSearch[].class) as HttpEntity<List>
            final List<ExperimentSearch> experiments = exchange.getBody()
            headers = exchange.getHeaders()
            this.extractETagsFromResponseHeader(headers, 0, etags)
            int nhits = experiments.size();

            final ExperimentSearchResult experimentResult = new ExperimentSearchResult()
            experimentResult.setExperiments(experiments)
            experimentResult.setEtags(etags)
            final MetaData metaData = new MetaData()
            metaData.nhit = nhits
            experimentResult.setMetaData(metaData)
            return experimentResult
        }
        return null

    }

    public ExperimentSearchResult findExperimentsByFreeTextSearch(SearchParams searchParams) {
        final String urlString = this.buildSearchURL(searchParams)
        final URL url = new URL(urlString)
        final ExperimentSearchResult experimentSearchResult = (ExperimentSearchResult)this.getForObject(url.toURI(), ExperimentSearchResult)
        return experimentSearchResult
    }

    /**
     *
     * @param capIds
     * @param searchParams
     * @param expandedSearch
     * @return
     */
    public ExperimentSearchResult searchExperimentsByCapIds(final List<Long> capIds, final SearchParams searchParams, final boolean expandedSearch = true) {
        if (capIds) {
            final Map<String, Long> etags = [:]
            final long skip = searchParams.getSkip()
            HttpHeaders requestHeaders = new HttpHeaders();
            HttpEntity<List> entity = new HttpEntity<List>(requestHeaders);


            final String urlString = buildSearchByCapIdURLs(capIds, searchParams, "capExptId:")
            urlString.replaceAll("true", expandedSearch.toString())
            final URL url = new URL(urlString)
            final HttpEntity<ExperimentSearchResult> exchange = getExchange(url.toURI(), entity, ExperimentSearchResult.class) as HttpEntity<ExperimentSearchResult>
            final ExperimentSearchResult experimentSearchResult = exchange.getBody()

            final HttpHeaders headers = exchange.getHeaders()
            extractETagsFromResponseHeader(headers, skip, etags)
            experimentSearchResult.setEtags(etags)
            return experimentSearchResult
        }

        return null

    }

    public ExperimentData activitiesByCIDsAndEIDs(final List<Long> cids, final List<Long> eids, int maximumValues = 500) {
        final SearchParams searchParams = new SearchParams(skip: 0, top: maximumValues)
        if ((cids) && (eids)) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("cids", cids.join(","));
            map.add("eids", eids.join(","));
            final String urlString = buildURLToExperimentData(searchParams)
            final URL url = new URL(urlString)
            List<Activity> activities = null
            try {
                activities = this.postForObject(url.toURI(), Activity[].class, map) as List<Activity>;
            } catch (HttpClientErrorException hce) {
                println hce.toString()
                // the NCGC rest API uses a 404 error to indicate no data found. This is a legitimate condition,
                // not an error, so we can swallow the HTTP client exception in this case
            }
            ExperimentData experimentData = new ExperimentData()
            experimentData.setActivities(activities)
            return experimentData
        }
        return null
    }

    public ExperimentData activitiesBySIDsAndEIDs(final List<Long> sids, final List<Long> eids, int maximumValues = 500) {
        final SearchParams searchParams = new SearchParams(skip: 0, top: maximumValues)
        if ((sids) && (eids)) {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("sids", sids.join(","));
            map.add("eids", eids.join(","));
            final String urlString = buildURLToExperimentData(searchParams)
            final URL url = new URL(urlString)
            final List<Activity> activities = this.postForObject(url.toURI(), Activity[].class, map) as List<Activity>;
            ExperimentData experimentData = new ExperimentData()
            experimentData.setActivities(activities)
            return experimentData
        }
        return null
    }

    public ExperimentData activities(Long experimentId) {
        return activities(experimentId, null)
    }

    public ExperimentData activities(final Long experimentId, final String etag) {
        // unbounded fetching
        Integer top = multiplier * multiplier;
        int ratio = multiplier;
        Integer skip = 0;
        final List<Activity> activities = []
        ExperimentData experimentData = new ExperimentData()
        while (true) {
            final String resource = buildExperimentQuery(experimentId, etag, top, skip, [FilterTypes.TESTED]);
            final URL url = new URL(resource)
            int currentSize
            List<Activity> currentActivities
            if (etag) {
                currentActivities = (getForObject(url.toURI(), Activity[].class)) as List<Activity>

            } else {
                final ExperimentData currentExperimentData = (ExperimentData) this.getForObject(url.toURI(), ExperimentData.class)
                currentActivities = currentExperimentData.activities
            }
            if (currentActivities) {
                currentSize = currentActivities.size()
                activities.addAll(currentActivities);
            } else {
                currentSize = 0
            }
            if (currentSize < top || activities.size() > RestApiConstants.MAXIMUM_NUMBER_OF_EXPERIMENTS) {
                break; // we're done
            }
            skip += currentSize;
            ratio *= multiplier;
            top = findNextTopValue(skip, ratio);

        }
        experimentData.setActivities(activities)
        return experimentData;
    }
    /**
     *
     * @param experimentId
     * @param etag
     * @param top
     * @param skip
     * @return ExperimentData
     */
    public ExperimentData activities(Long experimentId, String etag, Integer top, Integer skip, List<FilterTypes> filterType) {
        final String resource = buildExperimentQuery(experimentId, etag, top, skip, filterType)
        final URL url = new URL(resource)
        ExperimentData experimentData

        if (etag) {
            experimentData = new ExperimentData()
            final List<Activity> activities = (this.getForObject(url.toURI(), Activity[].class)) as List<Activity>
            experimentData.setActivities(activities)
        } else {
            experimentData = (ExperimentData) this.getForObject(url.toURI(), ExperimentData.class)
        }
        return experimentData
    }
    /**
     *
     * @return a url prefix for free text compound searches
     */
    @Override
    public String getSearchResource() {
        final String resourceName = getResourceContext()
        return new StringBuilder(externalUrlDTO.ncgcUrl).
                append(RestApiConstants.FORWARD_SLASH).
                append(RestApiConstants.SEARCH).
                append(resourceName).
                append(RestApiConstants.FORWARD_SLASH).
                append(RestApiConstants.QUESTION_MARK).
                toString();
    }

    @Override
    public String getResource() {
        final String resourceName = getResourceContext()
        return new StringBuilder(externalUrlDTO.ncgcUrl).
                append(resourceName).
                append(RestApiConstants.FORWARD_SLASH).
                toString();
    }



    public long getExptDataCount() {
        return this.getResourceCount(getExptDataResourceCountURL())
    }

    public String getExptDataResourceCountURL() {
        final String resourceName = RestApiConstants.EXPTDATA_RESOURCE
        return new StringBuilder(externalUrlDTO.ncgcUrl).
                append(resourceName).
                append(RestApiConstants.FORWARD_SLASH).
                append(RestApiConstants._COUNT).
                toString();
    }

    public CompoundResult findCompoundsByExperimentId(final Long eid) {
        final StringBuilder resource =
            new StringBuilder(
                    this.getResource(eid.toString())).
                    append(RestApiConstants.COMPOUNDS_RESOURCE).
                    append(RestApiConstants.QUESTION_MARK).
                    append(RestApiConstants.EXPAND_TRUE)
        final URL url = new URL(resource.toString())
        CompoundResult compoundResult = (CompoundResult) this.getForObject(url.toURI(), CompoundResult.class)
        return compoundResult
    }

    public ProjectResult findProjectsByExperimentId(final Long eid) {
        final StringBuilder resource =
            new StringBuilder(
                    this.getResource(eid.toString())).
                    append(RestApiConstants.PROJECTS_RESOURCE).
                    append(RestApiConstants.QUESTION_MARK).
                    append(RestApiConstants.EXPAND_TRUE)
        final URL url = new URL(resource.toString())
        final ProjectResult projectResult = (ProjectResult) this.getForObject(url.toURI(), ProjectResult.class)
        return projectResult
    }

    public List<Long> compoundsForExperiment(Long experimentId) {
        String resource = this.getResource(experimentId.toString()) + RestApiConstants.COMPOUNDS_RESOURCE;
        final URL url = new URL(resource)
        Map<String, Object> response = (Map) this.getForObject(url.toURI(), Map.class)
        List<String> compoundURLs = (List<String>) response.get("collection")
        if (compoundURLs) {
            return compoundURLs.collect { String s -> new Long(s.substring(s.lastIndexOf("/") + 1).trim()) }
        }
        return []
    }

}
