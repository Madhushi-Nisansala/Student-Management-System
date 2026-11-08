import axios from 'axios';

const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api' });
const attendance = '/attendance';

const handle = (promise) => promise.catch((error) => { throw new Error(error.response?.data?.message || 'Unable to complete the request.'); });
export const getAllAttendance = (params = {}) => handle(api.get(attendance, { params }));
export const getAttendanceById = (id) => handle(api.get(`${attendance}/${id}`));
export const createAttendance = (data) => handle(api.post(attendance, data));
export const updateAttendance = (id, data) => handle(api.put(`${attendance}/${id}`, data));
export const deleteAttendance = (id) => handle(api.delete(`${attendance}/${id}`));
export const getAttendanceByStudent = (id) => handle(api.get(`${attendance}/student/${id}`));
export const getAttendanceBySubject = (id) => handle(api.get(`${attendance}/subject/${id}`));
export const getAttendanceSummary = (type, id) => handle(api.get(`${attendance}/summary/${type}/${id}`));
export const getStudents = () => handle(api.get('/students'));
export const getSubjects = () => handle(api.get('/subjects'));
export default api;
