<div class="tab-pane active" id="tab-definitions">
    <div class="items-gallery slide" id="items-gallery-2" data-interval="false">
        <a href="#items-gallery-2" class="btn-prev" data-slide="prev" data-toggle="collapse">Previous</a>
        <a href="#items-gallery-2" class="btn-next" data-slide="next">Previous</a>

        <div class="carousel-inner">
            <div class="item active">
                <div class="row-fluid">
                    <g:each status="i" in="${recentlyAddedAssays}" var="assay">
                        <g:if test="${i < 3}">
                            <g:render template="recentlyAddedAssay" model="['assay':assay]"/>
                        </g:if>
                    </g:each>
                </div>
            </div>

            <div class="item">
                <div class="row-fluid">
                    <g:each status="i" in="${recentlyAddedAssays}" var="assay">
                        <g:if test="${i >= 3}">
                            <g:render template="recentlyAddedAssay" model="['assay':assay]"/>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </div>
    </div>
</div>