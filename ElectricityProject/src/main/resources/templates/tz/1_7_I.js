window.onload = function() {
    // 页面完全加载后执行的代码
    console.log('所有资源加载完毕');

    function zhixing(){
        $.ajax({
            url: '/hdb/getBYId71',
            success: function (cms) {
                // console.log("这是前端传回的数据",JSON.stringify(cms,null,2));
                cms.forEach(item => {
                    // console.log("Name:", item.name);
                    // console.log("CT1:", item.CT1);

                    var elementById = document.getElementById(item.name+"-d");
                    var elementByIa = document.getElementById(item.name+"-Ia");
                    var elementByIb = document.getElementById(item.name+"-Ib");
                    var elementByIc = document.getElementById(item.name+"-Ic");
                    if (item.CT1 !== "0.0" && elementByIa){ //判断电表是否运行
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
    zhixing();
    setInterval(zhixing,5*6*1000)
};