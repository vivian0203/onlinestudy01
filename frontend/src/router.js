
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManagementManager from "./components/OrderManagementManager"

import LearningManagementManager from "./components/LearningManagementManager"

import LearningEvaluationManager from "./components/LearningEvaluationManager"


import 학습현황조회 from "./components/학습현황조회"
import SmsHistoryManager from "./components/SmsHistoryManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orderManagements',
                name: 'OrderManagementManager',
                component: OrderManagementManager
            },

            {
                path: '/learningManagements',
                name: 'LearningManagementManager',
                component: LearningManagementManager
            },

            {
                path: '/learningEvaluations',
                name: 'LearningEvaluationManager',
                component: LearningEvaluationManager
            },


            {
                path: '/학습현황조회',
                name: '학습현황조회',
                component: 학습현황조회
            },
            {
                path: '/smsHistories',
                name: 'SmsHistoryManager',
                component: SmsHistoryManager
            },



    ]
})
