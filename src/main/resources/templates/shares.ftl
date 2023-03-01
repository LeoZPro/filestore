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
    <#assign section="shares">
    <#include "menu.ftl">
    <section class="section-container">
        <div class="content-wrapper">
            Ici les partages... :!TODO!:
        </div>
    </section>
</div>
<script src="${content.ctx}/vendor/modernizr/modernizr.custom.js"></script>
<script src="${content.ctx}/vendor/jquery/dist/jquery.js"></script>
<script src="${content.ctx}/vendor/popper.js/dist/umd/popper.js"></script>
<script src="${content.ctx}/vendor/bootstrap/dist/js/bootstrap.js"></script>
<script src="${content.ctx}/vendor/js-storage/js.storage.js"></script>
<script src="${content.ctx}/vendor/jquery.easing/jquery.easing.js"></script>
<script src="${content.ctx}/vendor/animo/animo.js"></script>
<script src="${content.ctx}/vendor/screenfull/dist/screenfull.js"></script>
<script src="${content.ctx}/vendor/moment/min/moment-with-locales.js"></script>
<script src="${content.ctx}/js/app.js"></script>
</body>
</html>



