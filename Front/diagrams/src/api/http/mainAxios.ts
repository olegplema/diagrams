import axios from 'axios';

export const mainAxios = axios.create({
  withCredentials: true,
  baseURL: "http://localhost:8888" //process.env.REACT_APP_BASE_URL
});
