window.onload = function() {
    zhixing();
    // byq();
    // setInterval(zhixing,5*6*1000)
    // setInterval(byq,5*6*1000)
};
function zhixing(){
    // 页面完全加载后执行的代码
    $.ajax({
        url: 'http://192.168.1.227:8081/hdb/getTowBYId422',
        success: function (cms) {
            cms.forEach(item => {
                var elementById = document.getElementById(item.name+"-d");
                var elementByIa = document.getElementById(item.name+"-Ia");
                var elementByIb = document.getElementById(item.name+"-Ib");
                var elementByIc = document.getElementById(item.name+"-Ic");
                var elementByVa = document.getElementById(item.name+"-Ua");
                var elementByVb = document.getElementById(item.name+"-Ub");
                var elementByVc = document.getElementById(item.name+"-Uc");
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