window.onload = function() {
    zhixing();
    cfjstatus();
    byq();
    setInterval(zhixing,5*6*1000)
    setInterval(cfjstatus,5*6*1000)
    setInterval(byq,5*6*1000)
};
function byq(){
    $.ajax({
        url: "http://192.168.1.227:8081/fhsb/fhsbData",
        data: {
            wangguanhao: 300,
            shebeihao: [ 59, 60, 61]
        },
        dataType: "json",
        success: function (response) {
            const temperatureAT = response["4#T.AT"];
            const elementCa = document.getElementById("BYQ-4-Ca");
            if (elementCa && temperatureAT !== undefined) {
                elementCa.textContent = temperatureAT/10; // 或者进行必要的格式转换，如 temperatureAT.toFixed(2)
            } else {
                console.warn("未找到BYQ-4-Ca元素或4#T.AT数据");
            }

            // 获取 BYQ-4-Cb 对应的温度值并显示
            const temperatureBT = response["4#T.BT"];
            const elementCb = document.getElementById("BYQ-4-Cb");
            if (elementCb && temperatureBT !== undefined) {
                elementCb.textContent = temperatureBT/10;
            } else {
                console.warn("未找到BYQ-4-Cb元素或4#T.BT数据");
            }

            // 获取 BYQ-4-Cc 对应的温度值并显示
            const temperatureCT = response["4#T.CT"];
            const elementCc = document.getElementById("BYQ-4-Cc");
            if (elementCc && temperatureCT !== undefined) {
                elementCc.textContent = temperatureCT/10;
            } else {
                console.warn("未找到BYQ-4-Cc元素或4#T.CT数据");
            }
        }  
    });
    $.ajax({
        url: "http://192.168.1.227:8081/hdb/getbyqfz",
        data: {
            id: 4,
            name :"1号配电室"
        },
        dataType: "json",
        success: function (response) {
            const fuzai = response;
            const elementfuzai = document.getElementById("BYQ-4-r");
            if (elementfuzai && fuzai !== undefined) {
                elementfuzai.textContent = fuzai;
            } else {
                console.warn("未找到fuzai元素或数据");
            } 
        }  
    });
}
function zhixing(){
    $.ajax({
        url: 'http://192.168.1.227:8081/hdb/getBYId72',
        success: function (cms) {
            cms.forEach(item => {
                var elementById = document.getElementById(item.name+"-d");
                var elementByIa = document.getElementById(item.name+"-Ia");
                var elementByIb = document.getElementById(item.name+"-Ib");
                var elementByIc = document.getElementById(item.name+"-Ic");
                var elementByVa = document.getElementById(item.name+"-Ua");
                var elementByVb = document.getElementById(item.name+"-Ub");
                var elementByVc = document.getElementById(item.name+"-Uc");
                if (item.CT1 !== "0.0" && elementByIa){ //判断电表是否运行
                    if (elementByVa){
                        elementByVa.textContent =item.Uan;
                        elementByVb.textContent =item.Ubn;
                        elementByVc.textContent =item.Ucn;
                    }
                    if (item.Ia === 0){
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
}
function cfjstatus(){
    $.ajax({
        url: "http://192.168.1.227:8081/fhsb/fhsbData",
        data: {
            wangguanhao: 307,
            shebeihao: [559,560,561]
        },
        dataType: "json",
        success: function (response) {
            // 遍历返回的键值对
            for (let key in response) {
                // 解析键名，提取数字部分（如从"2AA6-1.DI2"中提取"2"）
                const diNumber = key.split('.DI')[1];
                if (diNumber) {
                    // 构建对应的元素ID（如"2AA6-d-2"）
                    const elementId = `4AA10-d-${diNumber}`;
                    const element = document.getElementById(elementId);
                    
                    if (element) {
                        // 获取状态值
                        const status = response[key];
                        // 设置颜色：0.0为绿色，1.0为红色
                        if (status === '1.0') {
                            element.style.fill = "#ff0000"; // 红色
                        } else if (status === '0.0') {
                            element.style.fill = "#00ff46"; // 绿色
                        }
                    } else {
                        console.warn(`未找到ID为${elementId}的元素`);
                    }
                }
            }
        },
        error: function(error) {
            console.error("获取柴发机数据失败:", error);
        }
    });
    $.ajax({
        url: "http://192.168.1.227:8081/hdb/getlbqByName",
        data: {
            name: "4AA14"
        },
        dataType: "json",
        success: function (response) {
            const Ia = response.bc_Ia;
            const Ib = response.bc_Ib;
            const Ic = response.bc_Ic;
            const elementIa = document.getElementById("4AA14-Ia");
            const elementIb = document.getElementById("4AA14-Ib");
            const elementIc = document.getElementById("4AA14-Ic");
            if (elementIa && Ia !== undefined) {
                elementIa.textContent = Ia;
            } else {
                console.warn("未找到Ia元素或数据");
            }
            if (elementIb && Ib !== undefined) {
                elementIb.textContent = Ib;
            } else {
                console.warn("未找到Ib元素或数据");
            }
            if (elementIc && Ic !== undefined) {
                elementIc.textContent = Ic;
            } else {
                console.warn("未找到Ic元素或数据");
            }
        }  
    });
}