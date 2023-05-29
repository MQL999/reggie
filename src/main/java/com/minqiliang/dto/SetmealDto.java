package com.minqiliang.dto;

import com.minqiliang.entity.Setmeal;
import com.minqiliang.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
