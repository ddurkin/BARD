package bard.db.experiment

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import bard.db.registration.AttributeType
import bard.db.registration.ExternalReference
import groovy.sql.Sql
import org.hibernate.classic.Session
import org.hibernate.jdbc.Work

import java.sql.Connection
import java.sql.SQLException

/**
 * Created with IntelliJ IDEA.
 * User: pmontgom
 * Date: 3/14/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
class PubchemReformatService {

    static class ResultMapContextColumn {
        String attribute;
        String tid;
        String qualifierTid
    }

    static class ResultMapRecord {
        Integer series
        String tid;
        String parentTid;

        String resultType
        String statsModifier

        String qualifierTid

        Set<ResultMapContextColumn> contextItemColumns = [] as Set;
        Map<String,String> staticContextItems = [:];

        @Override
        public String toString() {
            return "ResultMapRecord{" +
                    "series=" + series +
                    ", tid='" + tid + '\'' +
                    ", parentTid='" + parentTid + '\'' +
                    ", resultType='" + resultType + '\'' +
                    ", statsModifier='" + statsModifier + '\'' +
                    ", qualifierTid='" + qualifierTid + '\'' +
                    ", contextItemColumns=" + contextItemColumns +
                    ", staticContextItems=" + staticContextItems +
                    '}';
        }
    }

    static class CapCsvWriter {
        CSVWriter writer
        int rowCount = 0;
        int columnCount;
        Map<String, Integer> nameToIndex = [:];

        CapCsvWriter(Long experimentId, Map<String,String> experimentContextItems, List<String> dynamicColumns, Writer rawWriter) {
            writer = new CSVWriter(rawWriter)
            columnCount = dynamicColumns.size()
            for(int i=0;i<columnCount;i++) {
                nameToIndex.put(dynamicColumns[i], i)
            }

            writer.writeNext(["","Experiment ID",experimentId.toString()].toArray(new String[0]))
            experimentContextItems.each { k, v ->
                writer.writeNext(["",k,v].toArray(new String[0]))
            }
            writer.writeNext([].toArray(new String[0]))
            List header = ["Row #","Substance","Replicate #","Parent Row #"]
            header.addAll(dynamicColumns)
            writer.writeNext(header.toArray(new String[0]))
        }

        int addRow(Long substanceId, Integer parentRow, String replicate, Map<String,String> row) {
            rowCount++;
            int rowNumber = rowCount;

            List<String> cols = new ArrayList()
            for(int i=0;i<columnCount;i++)
                cols.add("")

            row.each{ k, v ->
                if (v != null && !(v instanceof String)) {
                    throw new RuntimeException("${k} -> ${v} but has class ${v.getClass()}")
                }
                if (nameToIndex.containsKey(k)) {
                    cols[nameToIndex[k]] = v
                }
            }

            List fullRow = [rowNumber.toString(), substanceId.toString(), replicate?.toString(), parentRow?.toString()]
            fullRow.addAll(cols)
            writer.writeNext(fullRow.toArray(new String[0]))

            return rowNumber
        }

        void close() {
            writer.close()
        }
    }

    static class ResultMap {
        Map<String, Collection<ResultMapRecord>> records = [:]

        List<Map<String,String>> getValues(Map<String,String> pubchemRow, String resultType, String statsModifier, String parentTid) {
            List<Map<String,String>> rows = []
            String label = makeLabel(resultType, statsModifier)
            Collection<ResultMapRecord> records = records.get(label).findAll { it.parentTid == parentTid }
            for(record in records) {
                Map<String,String> kvs = ["Replicate #": record.series?.toString(), "TID": record.tid]

                String measureValue = pubchemRow[record.tid]
                if (measureValue != null && measureValue.length() > 0)
                {
                    if (record.qualifierTid != null) {
                        measureValue = pubchemRow[record.qualifierTid]+measureValue
                    }
                    kvs[label] = measureValue

                    addContextValues(pubchemRow, record, kvs)
                    rows.add(kvs)
                }
            }
            return rows
        }

        void addContextValues(Map<String,String> pubchemRow,ResultMapRecord record, Map<String,String> kvs) {
            for(staticItem in record.staticContextItems.entrySet()) {
                kvs[staticItem.key] = staticItem.value
            }

            for(columnItem in record.contextItemColumns) {
                String value = pubchemRow[columnItem.tid]
                if (columnItem.qualifierTid != null) {
                    value = pubchemRow[columnItem.qualifierTid] + value;
                }
                kvs[columnItem.attribute] = value
            }
        }

        @Override
        public String toString() {
            return "ResultMap{" +
                    "records=" + records +
                    '}';
        }
    }


    static String makeLabel(String resultType, String statsModifier) {
        if (statsModifier != null)
            return "${resultType} (${statsModifier})"
        return resultType
    }

    void convertRow(Collection<ExperimentMeasure> measures, Long substanceId, Map<String,String> pubchemRow, ResultMap map, CapCsvWriter writer, Integer parentRow, String parentTid) {
        for(expMeasure in measures) {
            String resultType = expMeasure.measure.resultType.label
            String statsModifier = expMeasure.measure.statsModifier?.label

            List rows = map.getValues(pubchemRow, resultType, statsModifier, parentTid)
            for(row in rows) {
                int rowNumber = writer.addRow(substanceId, parentRow, row["Replicate #"], row)

                // write out the children
//            println("${rowNumber}: ${row}")
                convertRow(expMeasure.childMeasures, substanceId, pubchemRow, map, writer, rowNumber, row["TID"])
            }
        }
    }

    List constructCapColumns(Experiment experiment) {
        Set colNames = [] as Set

        experiment.experimentMeasures.each {
            // add measure names
            colNames.add(makeLabel(it.measure.resultType.label, it.measure.statsModifier?.label))
            // add context items
            it.measure.assayContextMeasures.each {
                it.assayContext.assayContextItems.findAll { it.attributeType != AttributeType.Fixed } .each {
                    colNames.add(it.attributeElement.label)
                }
            }
        }

        return new ArrayList(colNames)
    }

    public ResultMap convertToResultMap(List rows) {
        ResultMap map = new ResultMap()
        Map byTid = [:]
        List<ResultMapRecord>  records = [];

        // first pass: create all the items for result types
        for(row in rows) {
            if (row.RESULTTYPE != null) {
                assert row.CONTEXTTID == null || row.CONTEXTTID == row.TID
                ResultMapRecord record = new ResultMapRecord(series: row.SERIESNO,
                        tid: row.TID,
                        parentTid: row.PARENTTID?.toString(),
                        resultType: row.RESULTTYPE,
                        statsModifier: row.STATS_MODIFIER,
                        qualifierTid: row.QUALIFIERTID)
                records.add(record)

                if (row.CONTEXTITEM != null) {
                    record.staticContextItems[row.CONTEXTITEM] = row.CONCENTRATION?.toString()
                }
                if (row.ATTRIBUTE1 != null) {
                    record.staticContextItems[row.ATTRIBUTE1] = row.VALUE1
                }
                if (row.ATTRIBUTE2 != null) {
                    record.staticContextItems[row.ATTRIBUTE2] = row.VALUE2
                }

                byTid[record.tid.toString()] = record
            }
        }

        //second pass: associate context columns to rows
        for(row in rows) {
            if (row.CONTEXTTID != null && row.TID != row.CONTEXTTID) {
                assert row.RESULTTYPE == null
                ResultMapContextColumn col = new ResultMapContextColumn( attribute: row.CONTEXTITEM, tid: row.TID, qualifierTid: row.QUALIFIERTID)
                ResultMapRecord record = byTid[row.CONTEXTTID.toString()]
                record.contextItemColumns.add(col)
            }
        }

        map.records = records.groupBy {it.resultType}

        return map;
    }

    //TODO: fix concentration unit check
    public ResultMap loadMap(Connection connection, Long aid) {
        ResultMap map = new ResultMap()
        Sql sql = new Sql(connection)
        List rows = sql.rows("SELECT TID, TIDNAME, PARENTTID, RESULTTYPE, STATS_MODIFIER, CONTEXTTID, CONTEXTITEM, CONCENTRATION, CONCENTRATIONUNIT, PANELNO, ATTRIBUTE1, VALUE1, ATTRIBUTE2, VALUE2, SERIESNO, QUALIFIERTID FROM result_map WHERE AID = ?", [aid])
//            rows.add(it)
//            // check the units provided match what the dictionary is expecting
////        if (it.CONCENTRATIONUNIT != null) {
////            assert it.CONCENTRATION != null
////            if (it.CONTEXTITEM != null) {
////                Element element = Element.findByLabel(it.CONTEXTITEM)
////                assert element != null, "could not find ${it.CONTEXTITEM}"
////                assert element.unit.abbreviation == it.CONCENTRATIONUNIT
////            } else {
////                Element element = Element.findByLabel(it.RESULTTYPE)
////                assert element != null, "could not find ${it.RESULTTYPE}"
////                assert element.unit.abbreviation == it.CONCENTRATIONUNIT
////            }
////        }
//
//            assert it.PANELNO == null
////            def rec = new ResultMapRecord(
////                    tid: it.TID,
////                    tidName: it.TIDNAME,
////                    series : it.SERIESNO,
////                    attribute0 : it.CONTEXTITEM,
////                    value0Tid : it.TID == it.CONTEXTTID ? null : it.CONTEXTTID,
////                    value0 : it.CONCENTRATION,
////                    attribute1: it.ATTRIBUTE1,
////                    value1 : it.VALUE1,
////                    attribute2 : it.ATTRIBUTE2,
////                    value2 : it.VALUE2,
////                    resultType: it.RESULTTYPE,
////                    statsModifier: it.STATS_MODIFIER,
////                    parentTid: it.PARENTTID?.toString())
////
////            records.add(rec)
//        }

        map = convertToResultMap(rows)
        println("${map}")
        return map
    }

    public void convert(Experiment experiment, String pubchemFilename, String outputFilename, ResultMap map) {
        List dynamicColumns = constructCapColumns(experiment)

        Map expItems = (experiment.experimentContexts.collectMany { ExperimentContext context ->
            context.experimentContextItems.collect { ExperimentContextItem item ->
                [item.attributeElement.label, item.valueDisplay]
            }
        }).collectEntries()

        CapCsvWriter writer = new CapCsvWriter(experiment.id, expItems, dynamicColumns, new FileWriter(outputFilename))
        CSVReader reader = new CSVReader(new FileReader(pubchemFilename))
        List<String> header = reader.readNext()
        Collection<ExperimentMeasure> rootMeasures = experiment.experimentMeasures.findAll { it.parent == null }
        while(true) {
            List<String> row = reader.readNext()
            if (row == null)
                break

            Long substanceId = Long.parseLong(row[0])
            String outcome = row[3]
            String activity = row[4]

            Map pubchemRow = [:]
            pubchemRow["-1"] = outcome
            pubchemRow["0"] = activity
            for(int i=0;i<header.size();i++) {
                pubchemRow[header[i]] = row[i]
            }

            convertRow(rootMeasures, substanceId, pubchemRow, map, writer, null, null)
        }

        writer.close()
    }

    void convert(Long expId, String pubchemFilename, String outputFilename)  {
        ResultMap map;
        Experiment experiment = Experiment.get(expId)
        ExternalReference ref = experiment.getExternalReferences().find {it.externalSystem.systemName == "PubChem"}
        Long aid = Long.parseLong(ref.extAssayRef.replace("aid=", ""));
        Experiment.withSession { Session session ->
            session.doWork(new Work() {
                void execute(Connection connection) throws SQLException {
                    map = loadMap(connection, aid);
                }
            })
        }
        convert(experiment, pubchemFilename, outputFilename, map)
    }
}
