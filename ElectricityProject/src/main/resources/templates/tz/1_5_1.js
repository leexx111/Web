window.onload = function() {
    zhixing();
    cfjstatus();
    setInterval(zhixing,5*6*1000)
    setInterval(cfjstatus,5*6*1000)
}; 
// 页面完全加载后执行的代码
function zhixing(){
    $.ajax({
        url: 'http://192.168.1.227:8081/hdb/getBYId51',
        success: function (cms) {
            // console.log("这是前端传回的数据",JSON.stringify(cms,null,2));
            cms.forEach(item => {
                // console.log("Name:", item.name);
                // // console.log("Ia:", item.Ia);
                // // console.log("Ib:", item.Ib);
                // // console.log("Ic:", item.Ic);
                // // console.log("DI1:", item.DI1);
                // console.log("CT1:", item.CT1);

                var elementById = document.getElementById(item.name+"-d");
                var elementByIa = document.getElementById(item.name+"-Ia");
                var elementByIb = document.getElementById(item.name+"-Ib");
                var elementByIc = document.getElementById(item.name+"-Ic");
                if (item.CT1 !== '0.0'){ //判断电表是否运行
                    if (elementByIa && item.Ia === 0){
                        elementByIa.textContent ="0.0";
                    }else if (elementByIa) {
                        elementByIa.textContent =item.Ia;
                    }
                    if (elementByIb && item.Ib === 0){
                        elementByIb.textContent ="0.0";
                    }else if (elementByIb) {
                        elementByIb.textContent =item.Ib;
                    }
                    if (elementByIc && item.Ic === 0){
                        elementByIc.textContent ="0.0";
                    }else if (elementByIc) {
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
            wangguanhao: 303,
            shebeihao: [751,752,753]
        },
        dataType: "json",
        success: function (response) {
            console.log("返回的 Map 数据:", response);
            
            // 遍历返回的键值对
            for (let key in response) {
                // 解析键名，提取数字部分（如从"2AA6-1.DI2"中提取"2"）
                const diNumber = key.split('.DI')[1];
                if (diNumber) {
                    // 构建对应的元素ID（如"2AA6-d-2"）
                    const elementId = `2AA6-d-${diNumber}`;
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
}
