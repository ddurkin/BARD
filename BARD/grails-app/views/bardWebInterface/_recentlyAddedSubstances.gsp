<div class="tab-pane" id="tab-substances">
    <div class="items-gallery slide" id="items-gallery-4" data-interval="false">
        <a href="#items-gallery-4" class="btn-prev" data-slide="prev">Previous</a>
        <a href="#items-gallery-4" class="btn-next" data-slide="next">Previous</a>

        <div class="carousel-inner">
            <div class="item active">
                <div class="row-fluid">
                    <g:each status="i" in="${recentlyAddedSubstances}" var="substance">
                        <g:if test="${i < 3}">
                            <g:render template="recentlyAddedSubstance" model="['substance':substance]"/>
                        </g:if>
                    </g:each>
                </div>
            </div>

            <div class="item">
                <div class="row-fluid">
                    <g:each status="i" in="${recentlyAddedSubstances}" var="substance">
                        <g:if test="${i >= 3}">
                            <g:render template="recentlyAddedSubstance" model="['substance':substance]"/>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </div>
    </div>
</div>
