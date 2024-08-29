//Вывод цвета объекта
export const trClassNameFormat = (row, rowIdx) => {
    let color = rowIdx.event_class_color;
    let color2 = {padding: 0, borderTop: "var(--border)", borderRight: "var(--border)",
                  borderBottom: "var(--border)", position: "relative", background: color };
    return color2;
}

//Вывод номер объекта
export const nomberNameFormat = (row, rowIdx) => {        
        return rowIdx.num_obj;      
}

//Вывод имя объекта
export const nameFormat = (row, rowIdx) => {        
        return rowIdx.name_obj;      
}

//Вывод адрес объекта
export const adressNameFormat = (row, rowIdx) => {        
        return rowIdx.address_obj;      
}

//Вывод текст сообщения
export const eventsNameFormat = (row, rowIdx) => {        
        return rowIdx.text_event;      
}


export const channelFormatter = (cell, row) => {
    if (row.num_channel === 1) {
        return 'GPRS'
    }
    if (row.num_channel === 2) {
        return 'GSM'
    }
    if (row.num_channel === 3) {
        return 'Радио'
    }
    if (row.num_channel === 4) {
        return 'АРМ'
    }
    if (row.num_channel === 5) {
        return 'ETHERNET'
    }
}

export function dateFormatter(timestamp, delimiter) {
    const date = new Date(Number(timestamp));
    const year = date.getFullYear();
    const month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
    const day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
    return `${day}${delimiter}${month}${delimiter}${year}`;
}

export function dateFormatterWithDotDelimeter(timestamp) {
    const date = new Date(Number(timestamp));
    const year = date.getFullYear();
    const month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
    const day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
    return `${day}.${month}.${year}`;
}

export function timeFormatter(timestamp) {
    const date = new Date(Number(timestamp));
    const hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
    const minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
    const seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
    return `${hours}:${minutes}:${seconds}`;
}

export function inputDateFormatter(date) {
    const year = date.getFullYear();
    const month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
    const day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
    return `${year}-${month}-${day}`;
}

export function inputTimeFormatter(timestamp) {
    const createdStatusDate = new Date(Number(timestamp));
    const hours = createdStatusDate.getHours() < 10 ? "0" + createdStatusDate.getHours() : createdStatusDate.getHours();
    const minutes = createdStatusDate.getMinutes() < 10 ? "0" + createdStatusDate.getMinutes() : createdStatusDate.getMinutes();
    const seconds = createdStatusDate.getSeconds() < 10 ? "0" + createdStatusDate.getSeconds() : createdStatusDate.getSeconds();
    return `${hours}:${minutes}:${seconds}`;
}


