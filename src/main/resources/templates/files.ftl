<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <meta name="description" content="File Store Drive">
    <meta name="keywords" content="app, responsive, jquery, bootstrap, dashboard, admin">
    <link rel="icon" type="image/x-icon" href="${content.ctx}/favicon.ico">
    <title>File Store - Drive</title>

    <link rel="stylesheet" href="${content.ctx}/vendor/@fortawesome/fontawesome-free-webfonts/css/fa-brands.css">
    <link rel="stylesheet" href="${content.ctx}/vendor/@fortawesome/fontawesome-free-webfonts/css/fa-regular.css">
    <link rel="stylesheet" href="${content.ctx}/vendor/@fortawesome/fontawesome-free-webfonts/css/fa-solid.css">
    <link rel="stylesheet" href="${content.ctx}/vendor/@fortawesome/fontawesome-free-webfonts/css/fontawesome.css">
    <link rel="stylesheet" href="${content.ctx}/vendor/simple-line-icons/css/simple-line-icons.css">
    <link rel="stylesheet" href="${content.ctx}/vendor/animate.css/animate.css">
    <link rel="stylesheet" href="${content.ctx}/vendor/whirl/dist/whirl.css">
    <link rel="stylesheet" href="${content.ctx}/css/bootstrap.css" id="bscss">
    <link rel="stylesheet" href="${content.ctx}/css/app.css" id="maincss">
</head>

<body>
<div class="wrapper">
    <header class="topnavbar-wrapper">
        <nav class="navbar topnavbar">
            <div class="navbar-header">
                <a class="navbar-brand" href="#/">
                    <div class="brand-logo">
                        <img class="img-fluid" src="${content.ctx}/img/logo.png" alt="FileStore Logo">
                    </div>
                    <div class="brand-logo-collapsed">
                        <img class="img-fluid" src="${content.ctx}/img/logo-single.png" alt="FileStore Logo">
                    </div>
                </a>
            </div>
            <ul class="navbar-nav mr-auto flex-row">
                <li class="nav-item">
                    <a class="nav-link d-none d-md-block d-lg-block d-xl-block" href="#" data-trigger-resize="" data-toggle-state="aside-collapsed">
                        <em class="fas fa-bars"></em>
                    </a>
                    <a class="nav-link sidebar-toggle d-md-none" href="#" data-toggle-state="aside-toggled" data-no-persist="true">
                        <em class="fas fa-bars"></em>
                    </a>
                </li>
            </ul>
        </nav>
    </header>
    <#assign section="files">
    <#include "menu.ftl">
    <section class="section-container">
        <div class="content-wrapper">
            <div class="content-heading">
                <div>
                    <div class="btn-group" role="group">
                        <button class="btn btn-secondary" type="button">
                            <a href="${content.ctx}/api/files"><em class="fa fa-home"></em></a>
                        </button>
                        <#list content.path as path>
                            <button class="btn btn-secondary" type="button">
                                <a href="${content.ctx}/api/files/${path.id}/content">${path.name}</a>
                            </button>
                        </#list>
                    </div>
                </div>
                <div class="ml-auto">
                    <div class="btn-group" role="group">
                        <button class="btn btn-secondary" type="button" data-toggle="modal" data-target="#createFolderModal">
                            <em class="fa fa-folder"></em>
                        </button>
                        <button class="btn btn-secondary" type="button" data-toggle="modal" data-target="#uploadFileModal">
                            <em class="fa fa-upload"></em>
                        </button>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-12">
                    <table class="table table-striped w-100" id="filestable">
                        <thead>
                        <tr>
                            <th data-priority="1">Nom</th>
                            <th>Taille</th>
                            <th>Type</th>
                            <th class="sort-numeric">Date de création</th>
                            <th class="sort-numeric">Date de modification</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <#if !content.parent.isRoot()>
                        <tr>
                            <td>
                                <i class="fa fa-folder mr-2"></i>
                                <a href="${content.ctx}/api/files/${content.parent.parent}/content">..</a>
                            </td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                        </tr>
                        </#if>
                        <#list content.nodes as node>
                        <tr>
                            <td>
                                <i class="fa ${helper.mimetypeToIcon(node.mimetype)} mr-2"></i>
                                <#if node.isFolder() >
                                    <a href="${content.ctx}/api/files/${node.id}/content">${node.name}</a>
                                <#else>
                                    <a href="${content.ctx}/api/files/${node.id}/content?download=false">${node.name}</a>
                                </#if>
                            </td>
                            <#if node.isFolder() >
                                <td>${node.size} elements</td>
                            <#else>
                                <td>${helper.sizeToBytes(node.size, false)}</td>
                            </#if>
                            <td>${node.mimetype}</td>
                            <td>${node.creation?number_to_datetime}</td>
                            <td>${node.modification?number_to_datetime}</td>
                            <td>
                                <#if !node.isFolder() >
                                    <a href="${content.ctx}/api/files/${node.id}/content?download=true" class="mr-3"><i class="fas fa-download"></i></a>
                                    <a href="${content.ctx}/api/files/${node.id}/content" class="mr-3"><i class="fas fa-eye"></i></a>
                                </#if>
                                <#if node.getMimetype() != "application/zip" >
                                    <a href="#" onclick="submitPostLink('${node.id}')"><i class="fas fa-folder-open"></i></a>
                                    <form action="${content.ctx}/api/action/zip/${node.id}" enctype="multipart/form-data" name="${node.id}" method="post">
                                        <input type="hidden" name="${node.id}" value="this is my POST data">
                                    </form>
                                <#else>
                                    <a href="#" onclick="submitPostLink('${node.id}')"><i class="fas fa-folder"></i></a>
                                    <form action="${content.ctx}/api/action/zip/${node.id}" enctype="multipart/form-data" name="${node.id}" method="post">
                                        <input type="hidden" name="${node.id}" value="this is my POST data">
                                    </form>
                                </#if>
                                <script language=javascript>
                                    function submitPostLink(nodeId)
                                    {
                                        document.forms[nodeId].submit();
                                    }
                                </script>
                            </td>
                        </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </section>
</div>
<div id="createFolderModal" class="modal fade" role="dialog">
    <div class="modal-dialog">
        <form method="post" action="${content.ctx}/api/files/${content.parent.id}" enctype="multipart/form-data">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title align-content-center">Create new Folder</h4>
                </div>
                <div class="modal-body">
                    <div class="form-row">
                        <div class="input-group mb-3">
                            <div class="input-group-prepend">
                                <span class="input-group-text" id="basic-addon1">Folder name</span>
                            </div>
                            <input type="text" id="name" name="name" class="form-control" aria-label="Folder Name" aria-describedby="basic-addon1">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary" id="upload">Create</button>
                </div>
            </div>
        </form>
    </div>
</div>
<div id="uploadFileModal" class="modal fade" role="dialog">
    <div class="modal-dialog">
        <form method="post" action="${content.ctx}/api/files/${content.parent.id}" enctype="multipart/form-data">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Upload file</h4>
                </div>
                <div class="modal-body">
                    <div class="form-row">
                        <div class="input-group col-md-12">
                            <label class="input-group-btn">
                                <span class="btn btn-primary">
                                    Browse… <input type="file" style="display: none;" id="file" name="data">
                                </span>
                            </label>
                            <input type="text" class="form-control" id="filename" name="name" readonly="">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary" id="upload">Upload</button>
                </div>
            </div>
        </form>
    </div>
</div>
<script src="${content.ctx}/vendor/modernizr/modernizr.custom.js"></script>
<script src="${content.ctx}/vendor/jquery/dist/jquery.js"></script>
<script src="${content.ctx}/vendor/popper.js/dist/umd/popper.js"></script>
<script src="${content.ctx}/vendor/bootstrap/dist/js/bootstrap.js"></script>
<script src="${content.ctx}/vendor/js-storage/js.storage.js"></script>
<script src="${content.ctx}/vendor/jquery.easing/jquery.easing.js"></script>
<script src="${content.ctx}/vendor/animo/animo.js"></script>
<script src="${content.ctx}/vendor/screenfull/dist/screenfull.js"></script>
<script src="${content.ctx}/js/app.js"></script>
<script>
    $('#file').change(function() {
        console.log("called");
        var filename = $('#file').val().replace(/\\/g, '/').replace(/.*\//, '');
        console.log(filename);
        $('#filename').val(filename);
    });
</script>
</body>
</html>