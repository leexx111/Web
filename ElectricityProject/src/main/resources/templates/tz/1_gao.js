function chaxun() {
    $.ajax({
        url: "http://192.168.1.227:8081/fhsb/fhsbData",
        data: {
            wangguanhao: 300,
            shebeihao: [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
                        18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33,
                        34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
                        50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65,
                        66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80 ]
        },
        dataType: "json",
        success: function (response) {
            console.log("返回的 Map 数据:", response);
            // 定义需要处理的设备列表
            const devices = [
                { prefix: "AH2",  statusValue: "297.0", hasVoltage: true },
                { prefix: "AH4",  statusValue: "9.0", hasVoltage: false },
                { prefix: "AH5",  statusValue: "9.0", hasVoltage: false },
                { prefix: "AH6",  statusValue: "1.0", hasVoltage: false },
                { prefix: "AH7",  statusValue: "9.0", hasVoltage: false },
                { prefix: "AH8",  statusValue: "9.0", hasVoltage: false },
                { prefix: "AH10", statusValue: "8.0", hasVoltage: false },
                { prefix: "AH11", statusValue: "9.0", hasVoltage: false },
                { prefix: "AH12", statusValue: "8.0", hasVoltage: false },//坏的
                { prefix: "AH13", statusValue: "9.0", hasVoltage: false },
                { prefix: "AH15", statusValue: "264.0", hasVoltage: true }
            ];
            
            // 循环处理每个设备
            devices.forEach(device => {
                const { prefix, statusValue, hasVoltage } = device;
                
                // 处理状态颜色
                const statusElement = document.getElementById(`${prefix}-d`);
                if (statusElement) {
                    debugger
                    // 状态值等于设定值时显示红色，否则绿色
                    if (response[`${prefix}.STATUS`] === statusValue) {
                        statusElement.style.fill = "#ff0000"; // 红色
                    } else {
                        statusElement.style.fill = "#00ff46"; // 绿色
                    }
                }

                // 处理Ia电流值（除以100）
                const iaElement = document.getElementById(`${prefix}-Ia`);
                if (iaElement) {
                    const iaValue = response[`${prefix}.Ia`] !== undefined ? response[`${prefix}.Ia`] / 100 : "0.0";
                    iaElement.textContent = iaValue.toFixed(2); // 保留两位小数
                }

                // 处理Ib电流值（除以100）
                const ibElement = document.getElementById(`${prefix}-Ib`);
                if (ibElement) {
                    const ibValue = response[`${prefix}.Ib`] !== undefined ? response[`${prefix}.Ib`] / 100 : "0.0";
                    ibElement.textContent = ibValue.toFixed(2); // 保留两位小数
                }

                // 处理Ic电流值（除以100）
                const icElement = document.getElementById(`${prefix}-Ic`);
                if (icElement) {
                    const icValue = response[`${prefix}.Ic`] !== undefined ? response[`${prefix}.Ic`] / 100 : "0.0";
                    icElement.textContent = icValue.toFixed(2); // 保留两位小数
                }

                // 处理电压值（仅AH2和AH15）
                if (hasVoltage) {
                    // 处理Uab电压值
                    const uabElement = document.getElementById(`${prefix}-Uab`);
                    if (uabElement) {
                        const uabValue = response[`${prefix}.Uab`] !== undefined ? response[`${prefix}.Uab` ] / 100000  : "0.0";
                        uabElement.textContent = uabValue.toFixed(2); // 保留两位小数
                    }

                    // 处理Ubc电压值
                    const ubcElement = document.getElementById(`${prefix}-Ubc`);
                    if (ubcElement) {
                        const ubcValue = response[`${prefix}.Ubc`] !== undefined ? response[`${prefix}.Ubc` ] / 100000 : "0.0";
                        ubcElement.textContent = ubcValue.toFixed(2); // 保留两位小数
                    }

                    // 处理Uca电压值
                    const ucaElement = document.getElementById(`${prefix}-Uca`);
                    if (ucaElement) {
                        const ucaValue = response[`${prefix}.Uca`] !== undefined ? response[`${prefix}.Uca` ] / 100000 : "0.0";
                        ucaElement.textContent = ucaValue.toFixed(2); // 保留两位小数
                    }
                }
            });
        },
        error: function (error) {
            console.error("请求失败:", error);
        }
    });
}

window.onload = function() {
    chaxun(); // 页面加载完成后调用
    // 每5秒刷新一次数据
    setInterval(chaxun, 50000);
}
