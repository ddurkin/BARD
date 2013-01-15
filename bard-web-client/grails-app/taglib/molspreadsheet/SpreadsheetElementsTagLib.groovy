package molspreadsheet

class SpreadsheetElementsTagLib {

    def imageCell = { attrs, body ->
        if  (attrs.smiles == 'Unknown smiles') {
            out <<  attrs.smiles
        } else {
            out <<  render(template: "/tagLibTemplates/compoundOptions", model: [cid: attrs.cid,
                    smiles: attrs.smiles, sid: attrs.sid,
                    imageWidth:"150", imageHeight:"120"])
        }
        }

        def cidCell =  { attrs, body ->
            out << """<a href="${this.createLink(controller:'bardWebInterface', action: 'showCompound', id: attrs.cid)}">${attrs.cid}</a>"""
        }


       def promiscuityCell = {  attrs, body ->
           out << """<div class="promiscuity"
                     href="${this.createLink(controller:'bardWebInterface', action: 'promiscuity', params: [cid: attrs.cid])}"
                     id="${attrs.cid}_prom"></div>"""
       }

    def activeVrsTestedCell = {   attrs, body ->
        out << """<div>
                      <span class="badge badge-info">${attrs.activeVrsTested}</span>
                  </div>"""
    }


    def exptDataCell  = {   attrs, body ->
        int currentCol = attrs.colCnt
        if (attrs.spreadSheetActivityStorage != null) {
            HillCurveValueHolder hillCurveValueHolder = attrs.spreadSheetActivityStorage.getHillCurveValueHolderList()[0]
            out << """<td class="molSpreadSheet" property="var${currentCol}">
                      <p>"""
            String mandatePopover = ""
            if (hillCurveValueHolder?.conc?.size() > 1) {
                mandatePopover = "drc-popover-link "
            }
            if (hillCurveValueHolder?.identifier) {
                out << """<div data-detail-id="drc_${attrs.spreadSheetActivityStorage.sid}_${currentCol}"
                               class="${mandatePopover}btn btn-link"
                               data-original-title="${hillCurveValueHolder.identifier}"
                               data-html="true"
                               data-trigger="hover">
                          ${hillCurveValueHolder.toString()}</div>"""
            }
            out << """</p>"""
            if (hillCurveValueHolder?.conc?.size() > 1) {
                out << """<div class='popover-content-wrapper'
                              id="drc_${attrs.spreadSheetActivityStorage.sid}_${currentCol}"
                              style="display: none;">
                              <div class="center-aligned">
                                   <img alt="${attrs.spreadSheetActivityStorage.sid}"
                                        title="Substance Id : ${attrs.spreadSheetActivityStorage.sid}"
                                        src="${this.createLink(
                                                   controller: 'doseResponseCurve',
                                                   action: 'doseResponseCurve',
                                                    params: [
                                                            sinf: hillCurveValueHolder.sInf,
                                                            s0: hillCurveValueHolder.s0,
                                                            ac50: hillCurveValueHolder.slope,
                                                            hillSlope: hillCurveValueHolder.coef,
                                                            concentrations: hillCurveValueHolder.conc,
                                                            activities: hillCurveValueHolder.response,
                                                            yAxisLabel: hillCurveValueHolder.identifier
                                                     ]
                                                    )}"/>
                               </div>
                           </div>
                        """
            }
        }  else {
            out << """<td class="molSpreadSheet" property="var${currentCol}">
                          Not tested in this experiment
                      </td>"""
        }


    }
}