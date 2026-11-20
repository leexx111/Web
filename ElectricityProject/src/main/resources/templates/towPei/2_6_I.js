window.onload = function() {
    zhixing();
    byq();
    // setInterval(zhixing,5*6*1000)
    // setInterval(byq,5*6*1000)
};
function byq(){
    $.ajax({
        url: "http://192.168.1.227:8081/fhsb/fhsbData",
        data: {
            wangguanhao: 319,
            shebeihao: [ 30,31,32,66, 67, 68 ]
        },
        dataType: "json",
        success: function (response) {
            const temperatureAT = response["2-3#T.AT"];
            const elementCa = document.getElementById("text3908-0-1-1-61");
            if (elementCa && temperatureAT !== undefined) {
                elementCa.textContent = temperatureAT/10; // 或者进行必要的格式转换，如 temperatureAT.toFixed(2)
            } else {
                console.warn("未找到BYQ-1-Ca元素或1#T.AT数据");
            }

            const LBQAT = response["2-3AA5.Ia"];
            const xltIa = document.getElementById("2-3AA5-1-Ia");
            if (xltIa && LBQAT !== undefined) {
                xltIa.textContent = LBQAT; // 或者进行必要的格式转换，如 temperatureAT.toFixed(2)
            } else {
                console.warn("未找到BYQ-1-Ca元素或1#T.AT数据");
            }
            const LBQbT = response["2-3AA5.Ib"];
            const xltIb = document.getElementById("2-3AA5-1-Ib");
            if (xltIb && LBQbT !== undefined) {
                xltIb.textContent = LBQbT; // 或者进行必要的格式转换，如 temperatureAT.toFixed(2)
            } else {
                console.warn("未找到BYQ-1-Ca元素或1#T.AT数据");
            }
            const LBQcT = response["2-3AA5.Ic"];
            const xltIc = document.getElementById("2-3AA5-1-Ic");
            if (xltIc && LBQcT !== undefined) {
                xltIc.textContent = LBQcT; // 或者进行必要的格式转换，如 temperatureAT.toFixed(2)
            } else {
                console.warn("未找到BYQ-1-Ca元素或1#T.AT数据");
            }

            // 获取 BYQ-1-Cb 对应的温度值并显示
            const temperatureBT = response["2-3#T.BT"];
            const elementCb = document.getElementById("text3908-1-2-7-32");
            if (elementCb && temperatureBT !== undefined) {
                elementCb.textContent = temperatureBT/10;
            } else {
                console.warn("未找到BYQ-1-Cb元素或1#T.BT数据");
            }

            // 获取 BYQ-1-Cc 对应的温度值并显示
            const temperatureCT = response["2-3#T.CT"];
            const elementCc = document.getElementById("text3908-7-2-1");
            if (elementCc && temperatureCT !== undefined) {
                elementCc.textContent = temperatureCT/10;
            } else {
                console.warn("未找到BYQ-1-Cc元素或1#T.CT数据");
            }
            document.getElementById("2-3AA3-1-Ia-3").textContent ="0.0";
            document.getElementById("2-3AA4-1-Ia-3").textContent ="0.0";
        }
    });
    $.ajax({
        url: "http://192.168.1.227:8081/hdb/getbyqfz",
        data: {
            id: 3,
            name :"2号配电室"
        },
        dataType: "json",
        success: function (response) {
            const fuzai = response;
            const elementfuzai = document.getElementById("text3908-7-2-1-8");
            if (elementfuzai && fuzai !== undefined) {
                elementfuzai.textContent = fuzai;
            } else {
                console.warn("未找到fuzai元素或数据");
            }
        }
    });
}
function zhixing(){
    // 页面完全加载后执行的代码
    $.ajax({
        url: 'http://192.168.1.227:8081/hdb/getTowBYId61',
        success: function (cms) {
            cms.forEach(item => {
                var elementById = document.getElementById(item.name+"-d");
                var elementByIa = document.getElementById(item.name+"-Ia");
                var elementByIb = document.getElementById(item.name+"-Ib");
                var elementByIc = document.getElementById(item.name+"-Ic");
                var elementByVa = document.getElementById(item.name+"-Va");
                var elementByVb = document.getElementById(item.name+"-Vb");
                var elementByVc = document.getElementById(item.name+"-Vc");
                if (item.CT1 !== '0.0' && elementByIa){ //判断电表是否运行
                    if (elementByVa){
                        elementByVa.textContent =item.Uan;
                        elementByVb.textContent =item.Ubn;
                        elementByVc.textContent =item.Ucn;
                    }
                    if (item.Ia === 0 ){
                        elementByIa.textContent ="0.0";
                    }else {
                        elementByIa.textContent =item.Ia;
                    }
                    if (item.Ib === 0){
                        elementByIb.textContent ="0.0";
                    }else {
                        elementByIb.textContent =item.Ib;
                    }
                    if (item.Ic === 0){
                        elementByIc.textContent ="0.0";
                    }else {
                        elementByIc.textContent =item.Ic;
                    }
                    if (elementById){
                        if (item.DI1 === '1.0'){
                            elementById.style.fill ="#ff0000";
                        }else if (item.DI1 === '0.0'){
                            elementById.style.fill ="#00ff46";
                        }
                    }
                } else {
                    if (elementById){
                        elementById.style.fill ="#000000";
                    }
                }
            });

        },
        error: function (error) {
            console.error(error);
        }
    });
    // $.ajax({
    //     url: "http://192.168.1.227:8081/hdb/getlbqByName",
    //     data: {
    //         name: "1AA5"
    //     },
    //     dataType: "json",
    //     success: function (response) {
    //         const Ia = response.bc_Ia;
    //         const Ib = response.bc_Ib;
    //         const Ic = response.bc_Ic;
    //         const elementIa = document.getElementById("1AA5-Ia");
    //         const elementIb = document.getElementById("1AA5-Ib");
    //         const elementIc = document.getElementById("1AA5-Ic");
    //         if (elementIa && Ia !== undefined) {
    //             elementIa.textContent = Ia;
    //         } else {
    //             console.warn("未找到Ia元素或数据");
    //         }
    //         if (elementIb && Ib !== undefined) {
    //             elementIb.textContent = Ib;
    //         } else {
    //             console.warn("未找到Ib元素或数据");
    //         }
    //         if (elementIc && Ic !== undefined) {
    //             elementIc.textContent = Ic;
    //         } else {
    //             console.warn("未找到Ic元素或数据");
    //         }
    //     }
    // });
}