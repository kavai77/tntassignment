package com.himadri.tnt.entity;

import java.util.List;

public interface ApiResponse {
   ApiResponse filterOnParams(List<String> params);
}
