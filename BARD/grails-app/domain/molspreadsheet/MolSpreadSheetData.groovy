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

package molspreadsheet

import bard.db.experiment.Experiment
import bard.db.registration.Assay

class MolSpreadSheetData {

    static hasMany = [molSpreadSheetCell: MolSpreadSheetCell]
    static transients = ['rowCount', 'columnCount']

    List<MolSpreadSheetColumnHeader> mssHeaders = []
    List<String> experimentNameList = []
    List<String> experimentFullNameList = []

    Map<String, MolSpreadSheetCell> mssData = [:]
    Map<Long, Integer> columnPointer = [:]
    Map<Long, Integer> rowPointer = [:]
    Map<Long, Long> mapExperimentIdsToCapAssayIds = [:]
    Map<Integer, String> mapColumnsToAssay = [:]
    Map<Integer, String> mapColumnsToExperimentId = [:]
    Map<Integer, String> mapColumnsToAssayName = [:]
    Map<Integer, Boolean> mapColumnsNormalization = [:]
    Map<Long, String> mapCapAssayIdsToAssayNames = [:]

    MolSpreadsheetDerivedMethod molSpreadsheetDerivedMethod

    static mapping = {
        table('MOL_SS_DATA')
        id(generator: 'sequence', params: [sequence: 'MOL_SS_DATA_ID_SEQ'])
        mssHeaders(indexColumn: [name: "MOL_SS_COL_HEADER_IDX"])
        experimentNameList(joinTable: [name: 'MOL_SS_DATA_EXP_NAME_LIST', key: 'MOL_SS_DATA_ID', column: "EXP_NAME"], indexColumn: [name: "EXP_NAME_IDX"])
        experimentFullNameList(joinTable: [name: 'MOL_SS_DATA_EXP_FULL_NAME_LIST', key: 'MOL_SS_DATA_ID', column: "EXP_FULL_NAME"], indexColumn: [name: "EXP_FULL_NAME_IDX"])

        mssData(joinTable: [name: 'MOL_SS_DATA_MOL_SS_CELL_MAP', key: 'MOL_SS_DATA_ID', column: "MOL_SS_CELL_ID"], indexColumn: [name: "CELL_POSITION_IDX", type: String])
        columnPointer(joinTable: [name: 'MOL_SS_DATA_COLUMN_POINTER_MAP', key: 'MOL_SS_DATA_ID', column: "COLUMN_POINTER"], indexColumn: [name: "COLUMN_POINTER_IDX", type: Long])
        rowPointer(joinTable: [name: 'MOL_SS_DATA_ROW_POINTER_MAP', key: 'MOL_SS_DATA_ID', column: "ROW_POINTER"], indexColumn: [name: "ROW_POINTER_IDX", type: Long])
        mapExperimentIdsToCapAssayIds(joinTable: [name: 'MOL_SS_DATA_EXP_CAP_MAP', key: 'MOL_SS_DATA_ID', column: "CAP_ASSAY_ID"], indexColumn: [name: "EXP_ID_IDX", type: Long])
        mapColumnsToAssay(joinTable: [name: 'MOL_SS_DATA_COL_ASSAY_MAP', key: 'MOL_SS_DATA_ID', column: "ASSAY"], indexColumn: [name: "COLUMN_ID_IDX", type: Integer])
        mapColumnsToAssayName(joinTable: [name: 'MOL_SS_DATA_COL_ASSAY_NAME_MAP', key: 'MOL_SS_DATA_ID', column: "ASSAY_NAME"], indexColumn: [name: "COLUMN_ID_IDX", type: Integer])

        mapColumnsNormalization(joinTable: [name: 'MOL_SS_DATA_COLS_NORM_MAP', key: 'MOL_SS_DATA_ID', column: "NORMALIZATION"], indexColumn: [name: "COLUMN_ID_IDX", type: Integer])

        molSpreadsheetDerivedMethod(column: 'MOL_SS_DERIVED_METHOD')
    }


    static constraints = {
        mssData(nullable: false)
        rowPointer(nullable: false)
        columnPointer(nullable: false)
        molSpreadsheetDerivedMethod(nullable: true)
    }

    /**
     * Display a cell, as specified by a row and column
     * @param rowCnt
     * @param colCnt
     * @return
     */
    Map displayValue(int rowCnt, int colCnt) {
        Map<String, String> returnValue = [:]
        String key = "${rowCnt}_${colCnt}"
        MolSpreadSheetCell molSpreadSheetCell
        if (mssData.containsKey(key)) {
            molSpreadSheetCell = mssData[key]
            returnValue = molSpreadSheetCell.mapForMolecularSpreadsheet()
        } else {  // This is a critical error.  Try to cover all the bases so we don't crash at least.
            returnValue.put("value", "-")
            returnValue.put("name", "Unknown name")
            returnValue.put("smiles", "Unknown smiles")
        }
        returnValue
    }


    SpreadSheetActivityStorage findSpreadSheetActivity(int rowCnt, int colCnt) {
        SpreadSheetActivityStorage spreadSheetActivityStorage = null
        String key = "${rowCnt}_${colCnt}"
        MolSpreadSheetCell molSpreadSheetCell
        if (mssData.containsKey(key)) {
            molSpreadSheetCell = mssData[key]
            spreadSheetActivityStorage = molSpreadSheetCell.spreadSheetActivityStorage
        }
        return spreadSheetActivityStorage
    }

    /**
     *
     * @return
     */
    int getRowCount() {
        if (rowPointer) {
            return rowPointer.size()
        }
        return 0
    }

    /**
     *
     * @return
     */
    int getColumnCount() {
        if (mssHeaders) {
            return getColumns().size()
        }
        return 0

    }


    int getSuperColumnCount() {
        if (mssHeaders) {
            return mssHeaders.size()
        }
        return 0

    }

    List<String> getSubColumns(int experimentCount) {
        List<String> subColumns = []
        if (experimentCount < getSuperColumnCount())
            subColumns = this.mssHeaders[experimentCount].molSpreadSheetColSubHeaderList*.columnTitle
        subColumns
    }

    List<MolSpreadSheetColSubHeader> getSubColumnList(int experimentCount) {
        List<String> subColumns = []
        if (experimentCount < getSuperColumnCount())
            subColumns = this.mssHeaders[experimentCount].molSpreadSheetColSubHeaderList
        subColumns
    }


    List<String> getColumns() {
        if (mssHeaders) {
            return mssHeaders*.molSpreadSheetColSubHeaderList*.columnTitle.flatten()
        }
        return []

    }


    List<String> getColumnsDescr() {
        if (mssHeaders) {
            return mssHeaders*.molSpreadSheetColSubHeaderList*.unitsInColumn.flatten()
        }
        return []

    }


    List<LinkedHashMap<String, String>> determineExperimentPerAssay() {

        List<LinkedHashMap<String, Integer>> returnValue = []
        LinkedHashMap<String, Integer> accumulator = [:]
        for (element in mapColumnsToExperimentId) {
            if (!accumulator.containsKey("${element.value}".toString())) {
                accumulator["${element.value}"] = 1
            } else {
                accumulator["${element.value}"] = (1 + accumulator["${element.value}"])
            }
        }

        for (element in accumulator) {
            final String eid = element.key
            final Long experimentId = new Long(eid)
            String experimentStatus = null
            if (experimentId) {
                final Experiment experiment = Experiment.findById(experimentId)
                if (experiment) {
                    experimentStatus = experiment.experimentStatus.id
                }
            }
            returnValue << [eid: "${eid}", colspan: element.value, status: experimentStatus ?: ""]
        }

        returnValue
    }


    List<LinkedHashMap<String, String>> determineResponseTypesPerAssay() {
        List<LinkedHashMap<String, String>> returnValue = []
        LinkedHashMap<String, Integer> accumulator = [:]
        List<String> assayNames = []
        if (mapColumnsToAssay.size() > 4) {
            for (int i in 4..(mapColumnsToAssay.size() - 1)) {
                if (accumulator.containsKey(mapColumnsToAssay[i])) {
                    (accumulator[mapColumnsToAssay[i]])++
                } else {
                    (accumulator[mapColumnsToAssay[i]]) = 0
                    assayNames << mapColumnsToAssay[i]
                }
            }
        }

        if (assayNames) {
            for (int i in 0..(assayNames.size() - 1)) {
                String fullAssayName = 'Data error: please contact your system administrator'
                // This message should never be displayed
                if (assayNames[i] != null) {    // Assay name should never be null -- this is a safety measure
                    Long bardAssayId = Long.parseLong(assayNames[i])
                    if (this.mapCapAssayIdsToAssayNames.containsKey(bardAssayId)) {
                        fullAssayName = mapCapAssayIdsToAssayNames[bardAssayId]
                    } else {
                        fullAssayName = "Assay name unknown"
                    }
                }
                //convert assay id to cap id
                String capId = null
                if (assayNames[i]) {
                    Long assayId = assayNames[i].toLong()
                    if (mapExperimentIdsToCapAssayIds.containsKey(assayId)) {
                        capId = mapExperimentIdsToCapAssayIds[assayId].toString()
                    }
                }
                Boolean normalized = true
                String status = null
                if (capId) {
                    Assay assay = Assay.findById(new Long(capId))
                    if (assay) {
                        status = assay.assayStatus.id
                    }
                }
                if (mapColumnsNormalization.containsKey(assayNames[i])) {
                    normalized = mapColumnsNormalization[assayNames[i]]
                }

                returnValue << ["assayName": assayNames[i], "bardAssayId": capId ?: "U", "numberOfResultTypes": (accumulator[assayNames[i]] + 1), "fullAssayName": fullAssayName, "normalized": normalized, "status": status ?: ""]
            }
        }
        returnValue
    }


    void flipNormalizationForAdid(Long assayIdAsALong) {
        // We have the CAP ID.  First converted to the Bard assay ID
        Long bardAssayId = mapExperimentIdsToCapAssayIds.find { it.value == assayIdAsALong }?.key ?: 0L
        if (bardAssayId != 0) {
            String assayIdAsAString = bardAssayId as String
            if (mapColumnsNormalization.containsKey(assayIdAsAString)) {
                // if we don't recognize the Aid then ignore the request
                mapColumnsNormalization[assayIdAsAString] = !(mapColumnsNormalization[assayIdAsAString])
            }
        }
    }

    void flipNormalizationForAdid(String aIdToFlip) {
        Long assayIdAsALong = 0L
        try {
            assayIdAsALong = Long.valueOf(aIdToFlip)
        } catch (NumberFormatException numberFormatException) {
            // Since this parameter comes off the URL line we could get past anything.  If it isn't
            //  numerical then simply ignore it
        }
        if (assayIdAsALong != 0L) {
            flipNormalizationForAdid(assayIdAsALong)
        }
    }


}


enum MolSpreadsheetDerivedMethod {
    Compounds_NoAssays_NoProjects("Only compound(s); no assays or projects"),
    NoCompounds_Assays_NoProjects("Only assay(s); no compounds or projects"),
    NoCompounds_NoAssays_Projects("Only project(s); no assays or compounds");

    private final String description

    MolSpreadsheetDerivedMethod(String desc) {
        this.description = desc
    }

    public String description() {
        return description
    }
}
