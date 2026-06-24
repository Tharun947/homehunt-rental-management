import { environment } from '../../../environments/environment';

export const API_URL = environment.apiUrl;
export const API_ORIGIN = API_URL.endsWith('/api') ? API_URL.slice(0, -4) : '';
