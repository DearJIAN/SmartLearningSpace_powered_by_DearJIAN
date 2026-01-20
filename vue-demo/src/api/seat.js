import request from '@/utils/request'

export function getSeatList() {
    return request({
        url: '/api/seat/list',
        method: 'get'
    })
}

export function reserveSeat(data) {
    return request({
        url: '/api/seat/reserve',
        method: 'post',
        data
    })
}

export function checkInSeat(data) {
    return request({
        url: '/api/seat/check-in',
        method: 'post',
        data
    })
}

export function renewSeat(data) {
    return request({
        url: '/api/seat/renew',
        method: 'post',
        data
    })
}

export function checkOutSeat(data) {
    return request({
        url: '/api/seat/check-out',
        method: 'post',
        data
    })
}

export function resetSeats() {
    return request({
        url: '/api/seat/reset',
        method: 'post'
    })
}

export function simulateSeats() {
    return request({
        url: '/api/seat/simulate',
        method: 'post'
    })
}
