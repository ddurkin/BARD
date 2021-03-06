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

package bardqueryapi

import bard.core.SearchParams
import bard.core.adapter.CompoundAdapter
import bard.core.rest.spring.assays.Assay
import bard.core.rest.spring.compounds.Compound
import bard.core.rest.spring.compounds.CompoundSummary
import bard.core.rest.spring.experiment.ExperimentSearch
import bard.core.rest.spring.project.Project
import bard.core.rest.spring.substances.Substance
import bard.core.rest.spring.util.StructureSearchParams
import bard.core.util.FilterTypes
import org.apache.commons.lang3.tuple.Pair

public interface IQueryService {
    Map<Long, Pair<Long, Long>> findActiveVsTestedForExperiments(final List<Long> capExperimentIds);

    String histogramDataByEID(long ncgcWarehouseId);

    long numberOfAssays()

    long numberOfProjects()

    long numberOfExperiments()

    long numberOfCompounds()

    long numberOfSubstances()

    long numberOfExperimentData()

    int numberOfProbeProjects()

    int numberOfProbeCompounds()

    Long totalNumberOfProbes()

    List<Long> findAllProbeProjects()

    Map findAllProbeCompounds()

    List<Assay> findRecentlyAddedAssays(int numberOfAssays)

    List<ExperimentSearch> findRecentlyAddedExperiments(int numberOfExperiments)

    List<Project> findRecentlyAddedProjects(int numberOfProjects)

    List<Substance> findRecentlyAddedSubstances(int numberOfSubstances)
    /**
     *
     * @param cid
     * @return {@link bard.core.rest.spring.compounds.CompoundSummary}
     */
    public CompoundSummary getSummaryForCompound(final Long cid);


    List<Long> findSubstancesByCid(Long cid);
    /**
     *
     * @param cid
     * return Map
     * Success would return [status: resp.status, message: 'Success', promiscuityScore: promiscuityScore]
     * Failure would return [status: HHTTP Error Code, message: "Error getting Promiscuity Score for ${fullURL}", promiscuityScore: null]
     */
    Map findPromiscuityScoreForCID(final Long cid);
    /**
     *
     * @param cid
     * return Map
     * Success would return [status: resp.status, message: 'Success', promiscuityScore: promiscuityScore]
     * Failure would return [status: HHTTP Error Code, message: "Error getting Promiscuity Score for ${fullURL}", promiscuityScore: null]
     */
    Map findPromiscuityForCID(final Long cid);

    //========================================================== Free Text Searches ================================
    /**
     * Find Compounds by Text search
     *
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map of results
     */
    Map findCompoundsByTextSearch(
            final String searchString, final Integer top, final Integer skip, final List<SearchFilter> searchFilters);
    /**
     *
     * @param cids
     * @param top
     * @param skip
     * @param searchFilters
     * @return Map of results
     */
    Map searchCompoundsByCids(
            final List<Long> cids, final Integer top, final Integer skip, final List<SearchFilter> searchFilters);

    /**
     *
     * @param capAssayIds
     * @param top
     * @param skip
     * @param searchFilters
     * @return Map of results
     */
    Map findAssaysByCapIds(
            final List<Long> capAssayIds,
            final Integer top, final Integer skip, final List<SearchFilter> searchFilters);

    /**
     * @param capProjectIds
     * @param top
     * @param skip
     * @param searchFilters
     * @return Map of results
     */
    Map findProjectsByCapIds(
            final List<Long> capProjectIds,
            final Integer top, final Integer skip, final List<SearchFilter> searchFilters);

    /**
     * @param capExperimentIds
     * @param top
     * @param skip
     * @param searchFilters
     * @return Map of results
     */
//    Map findExperimentsByCapIds(
//            final List<Long> capExperimentIds,
//            final Integer top, final Integer skip, final List<SearchFilter> searchFilters);

    /**
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map of results
     */
    Map findAssaysByTextSearch(
            final String searchString, final Integer top, final Integer skip, final List<SearchFilter> searchFilters);

    /**
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map
     */
    Map findProjectsByTextSearch(
            final String searchString, final Integer top, final Integer skip, final List<SearchFilter> searchFilters);

    /**
     * @param searchString
     * @param top
     * @param skip
     * @param searchFilters {@link SearchFilter}'s
     * @return Map
     */
    Map findExperimentsByTextSearch(
            final String searchString, final Integer top, final Integer skip, final List<SearchFilter> searchFilters);

    //====================================== Structure Searches ========================================
    /**
     * @param cid
     * @param structureSearchParamsType {@link StructureSearchParams}
     * @param top
     * @param skip
     * @param nhits - The number of hits if we already have it
     * @return Map
     */
    Map structureSearch(Integer cid, StructureSearchParams.Type structureSearchParamsType, Double threshold, List<SearchFilter> searchFilters, Integer top, Integer skip, Integer nhits);

    Map showProbeList()
    /**
     * @param smiles
     * @param structureSearchParamsType {@link StructureSearchParams}
     * @param top
     * @param skip
     * @return Map
     */
    Map structureSearch(String smiles, StructureSearchParams.Type structureSearchParamsType, List<SearchFilter> searchFilters, Double threshold, Integer top, Integer skip, Integer nhits);

    //===================== Find Resources given a list of IDs ================================
    /**
     * Given a list of Compound Ids return all the compounds that were found
     * @param compoundIds
     * @param filters {@link SearchFilter}'s
     * @return Map
     */
    Map findCompoundsByCIDs(final List<Long> compoundIds, List<SearchFilter> filters);

    /**
     * Given a list of Assay Ids return all the assays that were found
     * @param assayIds
     * @param filters {@link SearchFilter}'s
     * @return map
     */
    Map findAssaysByADIDs(final List<Long> assayIds, List<SearchFilter> filters);
    /**
     * Used for Show Experiment Page. Perhaps we should move this to the Query Service
     * @param experimentId
     * @param top
     * @param skip
     * @return Map of data to use to display an experiment
     */
    Map findExperimentDataById(Long experimentId, Integer top, Integer skip, List<FilterTypes> filterTypes);

    /**
     *
     * Given a list of Project Ids return all the projects that were found
     * @param projectIds
     * @param filters {@link SearchFilter}'s
     * @return Map
     */
    Map findProjectsByPIDs(final List<Long> projectIds, List<SearchFilter> filters);

    //=============== Show Resources Given a Single ID ================

    /**
     * Given a CID, get detailed compound information from REST API
     * @param compoundId
     * @return {@link CompoundAdapter}
     */
    CompoundAdapter showCompound(final Long compoundId);

    /**
     * Given an assayId, get detailed Assay information from the REST API
     * @param assayId
     * @return Map
     */
    Map showAssay(final Long assayId);
    /**
     * Given a projectId, get detailed Project information from the JDO
     * @param projectId
     * @return Map
     */
    Map showProject(final Long projectId);

    //==============================================Auto Complete ======
    /**
     *
     * @param term
     * @return the list of maps to use for auto suggest
     */
    public List<Map<String, String>> autoComplete(final String term);
    /**
     *
     * Extract filters from the search string if any
     * @param searchFilters {@link SearchFilter}'s
     * @param searchString
     * @return list of filters from search String
     */
    public void findFiltersInSearchBox(final List<SearchFilter> searchFilters, final String searchString);

    public QueryHelperService getQueryHelperService()

    public CompoundAdapter findProbe(String mlNumber)

    TableModel createCompoundBioActivitySummaryDataTable(Long compoundId,
                                                         GroupByTypes groupTypes,
                                                         List<FilterTypes> filterTypes,
                                                         List<SearchFilter> appliedSearchFilters,
                                                         SearchParams searchParams)

    TableModel showExperimentalData(Long experimentId,
                                    GroupByTypes groupTypes,
                                    List<FilterTypes> filterTypes,
                                    SearchParams searchParams)

    Map getPathsForAssayFormat(String endNode)

    Map getPathsForAssayType(String endNode)
}
