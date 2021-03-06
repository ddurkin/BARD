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

databaseChangeLog = {

    changeSet(author: "ddurkin", id: "reset sequneces", dbms: 'oracle', context:'load-data,delete-data,load-reference',runAlways: 'true') {
        grailsChange {
            change {
                def sequenceNames = []
                sql.eachRow('''select SEQUENCE_NAME from user_sequences''') {row ->
                    sequenceNames.add(row.SEQUENCE_NAME)
                }
                def potentialTableNames = sequenceNames.collect { it - '_ID_SEQ' }
                for (String sequenceName in sequenceNames) {
                    String tableName = sequenceName - '_ID_SEQ'
                    String maxIdQuery = "select max(${tableName}_ID) as max_id from ${tableName}"
                    try {
                        int maxId = (sql.rows(maxIdQuery)[0].max_id ?: 0) as int
                        int startWithVal = maxId + 1

                        String dropSeqSql = "drop sequence ${sequenceName}"
                        sql.execute(dropSeqSql)

                        String alterSeqSql = "create sequence ${sequenceName} increment by 1 start with ${startWithVal} maxvalue 2147483648 cache 2"
                        sql.execute(alterSeqSql)
                        println("The $tableName table had a max ${tableName}_ID of ${maxId}, the sequence ${sequenceName} was reset to start with ${startWithVal}")
                    }
                    catch (java.sql.SQLSyntaxErrorException e) {
                        //println(e.message)
                    }
                }
            }
        }
    }

}



