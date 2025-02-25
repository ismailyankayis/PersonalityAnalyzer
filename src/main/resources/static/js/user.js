var app = angular.module('UserApp', []);
app.controller('UserCtrl', function($scope,$http,$window) {

    $scope.userList = [];
    $scope.user = {};
    $scope.username = "";
    $scope.userId = "";
    $scope.detail = {};
    $scope.resuld_id = "";
    $scope.resultList = [];
    $scope.isLoading = false;
    $scope.errorMessage = "";
    $scope.account = {};
    $scope.id = "";
    $scope.password2 = "";
    $scope.accountList = [];

    $scope.findResultById = function(id){
        var usr = $http.get('/Result/getResultById/' + id);
        usr.then(function (response) {
            $scope.resultList.push(response.data);
        });
    }

    $scope.findUserByName = function () {
        var usr = $http.get('/User/findUserByUsername/' + $scope.username);
        usr.then(function (response) {
            $scope.user = response.data;
        });
    }

    $scope.analyzeButton = function () {
        $scope.isLoading = true;
        var usrResponse = $http.get('/User/analyzeButton/' + $scope.username);
        usrResponse.then(function (response) {
            var stringResponse = response.data;
            $scope.errorMessage = stringResponse.response;
            $scope.resuld_id = stringResponse.data;
            if($scope.resuld_id != null)
                $window.sessionStorage.setItem("result_id",$scope.resuld_id);
            if($scope.errorMessage === "Başarılı") {
                $scope.errorMessage = "";
                $window.sessionStorage.setItem("username",$scope.username);
                $window.sessionStorage.setItem("isLoading",$scope.isLoading);
                $scope.username = $window.sessionStorage.getItem("username");//session
                $scope.detail.username = $scope.username;
                //$scope.findAccountByUsername($scope.result.id);
                $scope.isLoading = true;
                $window.location.href = '/resultPage.html';
            }
            else {
                $scope.isLoading = false;
                alert($scope.errorMessage);
            }
        });
    }

});

