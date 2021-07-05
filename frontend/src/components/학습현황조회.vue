<template>

<v-data-table
    :headers="headers"
    :items="학습현황조회"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: '학습현황조회',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "orderNo", value: "orderNo" },
            { text: "courseNo", value: "courseNo" },
            { text: "phonenum", value: "phonenum" },
            { text: "orderdate", value: "orderdate" },
            { text: "begindate", value: "begindate" },
            { text: "enddate", value: "enddate" },
        ],
        학습현황조회 : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/학습현황조회s')

      this.학습현황조회 = temp.data._embedded.학습현황조회s;

    },
    methods: {
    }
  }
</script>

