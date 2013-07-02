function ChildList(villageBridge, childListBridge, cssIdOf) {
    var listView;

    var searchCriteria = function (child, searchString) {
        return (child.motherName.toUpperCase().indexOf(searchString) == 0
            || child.ecNumber.toUpperCase().indexOf(searchString) == 0
            || child.thayiCardNumber.toUpperCase().indexOf(searchString) == 0);
    };

    var villageFilterCriteria = function (child, appliedVillageFilter) {
        return child.villageName === appliedVillageFilter;
    };

    return {
        populateInto: function () {
            listView = new ListView(cssIdOf, Handlebars.templates.child_list, childListBridge.getChildren(), searchCriteria, villageFilterCriteria);

            var appliedVillageFilter = childListBridge.getAppliedVillageFilter(listView.ALL_VILLAGES_FILTER_OPTION);
            listView.filterByVillage(appliedVillageFilter, appliedVillageFilter);
        },
        bindEveryItemToChildView: function () {
            $(cssIdOf.rootElement).on("click", cssIdOf.everyListItem, function () {
                childListBridge.delegateToChildDetail($(this).data("caseid"));
            });
        },
        populateVillageFilter: function () {
            var villages = villageBridge.getVillages();
            villages.unshift({name: "All"});
            listView.populateVillageFilter(villages);
        },
        bindVillageFilterOptions: function () {
            listView.bindVillageFilterOptions();
            $(cssIdOf.villageFilterOptions).click(function () {
                childListBridge.delegateToSaveAppliedVillageFilter($(this).data(listView.VILLAGE_FILTER_OPTION));
            });
        },
        bindSearchEvents: function () {
            listView.bindSearchEvents();
        },
        bindLoadAll: function () {
            listView.bindLoadAll();
        }
    };
}

function ChildListBridge() {
    var childContext = window.context;
    if (typeof childContext === "undefined" && typeof FakeChildListContext !== "undefined") {
        childContext = new FakeChildListContext();
    }

    return {
        getChildren: function () {
            return JSON.parse(childContext.get());
        },
        delegateToChildDetail: function (caseId) {
            return childContext.startChild(caseId);
        },
        delegateToSaveAppliedVillageFilter: function (village) {
            return childContext.saveAppliedVillageFilter(village);
        },
        getAppliedVillageFilter: function (defaultFilterValue) {
            return childContext.appliedVillageFilter(defaultFilterValue);
        }
    };
}

function FakeChildListContext() {
    return {
        get: function () {
            return JSON.stringify({
                priority: [
                    {
                        caseId: "12345",
                        motherName: "Mother 1",
                        fatherName: "father 1",
                        thayiCardNumber: "TC Number 1",
                        villageName: "chikkabheriya",
                        hasTodos: true,
                        ecNumber: "EC 1",
                        isHighRisk: true
                    },
                    {
                        caseId: "11111",
                        motherName: "Mother 2",
                        fatherName: "father 2",
                        thayiCardNumber: "TC Number 2",
                        villageName: "munjanahalli",
                        ecNumber: "EC 2",
                        hasTodos: false,
                        isHighRisk: true
                    }
                ],
                normal: [
                    {
                        caseId: "12355",
                        motherName: "Mother 4",
                        fatherName: "father 4",
                        thayiCardNumber: "TC Number 4",
                        villageName: "chikkabheriya",
                        ecNumber: "EC 3",
                        hasTodos: true,
                        isHighRisk: false
                    },
                    {
                        caseId: "12355",
                        motherName: "Mother 5",
                        fatherName: "father 5",
                        thayiCardNumber: "TC Number 5",
                        villageName: "munjanahalli",
                        ecNumber: "EC 4",
                        hasTodos: false,
                        isHighRisk: false
                    },
                    {
                        caseId: "11121",
                        motherName: "Mother 6",
                        fatherName: "father 6",
                        thayiCardNumber: "TC Number 6",
                        ecNumber: "EC 5",
                        villageName: "chikkabheriya",
                        hasTodos: true,
                        isHighRisk: false
                    }
                ]
            });
        },
        startChild: function (caseId) {
            window.location.href = "child_detail.html";
        },
        saveAppliedVillageFilter: function (village) {
        },
        appliedVillageFilter: function (defaultFilterValue) {
            return defaultFilterValue;
        }
    }
}
