<div class="tab-pane" id="tab-projects" data-interval="false">
    <div class="items-gallery slide" id="items-gallery-1">
        <a href="#items-gallery-1" class="btn-prev" data-slide="prev">Previous</a>
        <a href="#items-gallery-1" class="btn-next" data-slide="next">Previous</a>

        <div class="carousel-inner">
            <div class="item active">
                <div class="row-fluid">
                    <g:each status="i" in="${recentlyAddedProjects}" var="project">
                        <g:if test="${i < 3}">
                            <g:render template="recentlyAddedProject" model="['project':project]"/>
                        </g:if>
                    </g:each>
                </div>
            </div>

            <div class="item">
                <div class="row-fluid">
                    <g:each status="i" in="${recentlyAddedProjects}" var="project">
                        <g:if test="${i >= 3}">
                            <g:render template="recentlyAddedProject" model="['project':project]"/>
                        </g:if>
                    </g:each>
                </div>
            </div>
        </div>
    </div>
</div>