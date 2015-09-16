#!/usr/bin/env python
# -*- coding: utf-8 -*-

from rest_framework import serializers
from base.serializers import BaseSerializer
from .models import PlanAuthor, PersonalPlan, Plan, Dish, Routine, SingleIngredient, SingleRecipe
from recipe.serializers import RecipeSerializer, IngredientSerializer

class PersonalPlanSerializer(BaseSerializer):
    class Meta:
        model = PersonalPlan


class PlanAuthorSerializer(BaseSerializer):
    class Meta:
        model = PlanAuthor


class SingleIngredientSerializer(BaseSerializer):
    ingredient = IngredientSerializer()

    class Meta:
        model = SingleIngredient


class SingleRecipeSerializer(BaseSerializer):
    recipe = RecipeSerializer(value=('id', 'nutrition_set', 'img', 'title', 'duration', 'calories', ))

    class Meta:
        model = SingleRecipe



class DishSerializer(BaseSerializer):
    singleingredient_set = SingleIngredientSerializer(many=True)
    singlerecipe_set = SingleRecipeSerializer(many=True)

    class Meta:
        model = Dish


class RoutineSerializer(BaseSerializer):
    dish_set = DishSerializer(many=True)

    class Meta:
        model = Routine


class PlanSerializer(BaseSerializer):
    routine = RoutineSerializer(many=True)
    author = PlanAuthorSerializer()

    class Meta:
        model = Plan

    def to_representation(self, obj):
        '''
        加上简单模式，用于列表展示
        '''
        r = super(PlanSerializer, self).to_representation(obj)
        simple = self.context.get('simple', True)
        if simple:
            # 去掉 component_set, procedure_set, nutrition_set
            pop_keys = ('routine',)
            for k in pop_keys:
                r.pop(k, None)
        return r
